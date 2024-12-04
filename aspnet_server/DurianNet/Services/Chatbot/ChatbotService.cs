using System.Text;
using LangChain.Chains;
using LangChain.Chains.HelperChains;
using LangChain.Databases;
using LangChain.Databases.Sqlite;
using LangChain.Extensions;
using LangChain.Memory;
using LangChain.Providers;
using LangChain.Providers.Ollama;
using Ollama;
using DurianNet.Utils;
using DurianNet.Dtos.Request.Chat;

namespace DurianNet.Services.Chatbot
{
    public class ChatbotService : IChatbotService
    {
        private readonly IEmbeddingModel _embeddingModel;
        private readonly OllamaChatModel _chatModel;
        private readonly IVectorDatabase _vectorDatabase;
        private readonly StackChain _chain;
        private readonly BaseChatMemory _memory;

        private readonly string _template;

        public ChatbotService()
        {
            var provider = new OllamaProvider();

            //_embeddingModel = new OllamaEmbeddingModel(provider, id: "all-minilm");
            _embeddingModel = new OllamaEmbeddingModel(provider, id: "nomic-embed-text");
            //_embeddingModel = new OllamaEmbeddingModel(provider, id: "mxbai-embed-large")

            _chatModel = new OllamaChatModel(provider, id: "llama3.1");

            var setting = new OllamaChatSettings()
            {
                Temperature = 0.0f,
                StopSequences = new string[] { "Human:", "Human: " },
                NumCtx = 4096,
                KeepAlive = -1,
                UseStreaming = true
            };
            _chatModel.Settings = setting;


            _vectorDatabase = new SqLiteVectorDatabase(dataSource: "vectors.db");

            _template = @" 
The following is a friendly and informative conversation between a Human and an AI. The AI is designed to respond to all queries in a structured, concise, and supportive tone. The AI's purpose is to assist the Human and other prospective users by offering accurate and helpful information exclusively about the Durian.  

### Guidelines for the AI:  
1. Scope of Responses: 
   - Provide information strictly related to durian.  
   - Do not offer personal opinions or unrelated information.  

2. Response Format:  
   - Use clear, concise, and friendly language.  
   - Structure responses for easy readability, such as lists or short paragraphs when appropriate.  

3. Tone:  
   - Maintain a supportive and professional tone that aligns with the durian.  

4. Context-Awareness:  
   - Respond based on the context and conversation history provided below.  

---

Context:  
{context} 

Conversation:
{history} 
Human: {input}
AI: ";

            _memory = GetChatMemory();

            _chain = Chain.LoadMemory(_memory, outputKey: "history")
                 | Chain.Template(_template)
                 | Chain.LLM(_chatModel, settings: new ChatSettings())
                 | Chain.UpdateMemory(_memory, requestKey: "input", responseKey: "text");

        }

        private string CleanResponse(string response)
        {
            // Remove Markdown formatting
            response = response.Replace("**", "");
            response = response.Replace("__", "");

            // Additional cleanup if necessary (e.g., trimming whitespace)
            return response.Trim();
        }

        public async Task<string> ChatAsync(string question, EventHandler<ChatResponseDelta> eventHandler)
        {
            _chatModel.DeltaReceived += eventHandler;

            Console.WriteLine("Chatting with the AI...");
            Console.WriteLine("Attempt to Getting vector collection...");
            var vectorCollection = await _vectorDatabase.GetOrCreateCollectionAsync("focs_clean", dimensions: 768);

            if (vectorCollection.IsEmptyAsync().Result)
            {
                Console.WriteLine("Vector Collections for focs is empty....");
                //await VectorDbUtils.DownloadWebsiteHMTLToVectorDB(_vectorDatabase, _embeddingModel);
                await VectorDbUtils.LoadTextFilesToVectorDB(_vectorDatabase, _embeddingModel);
            }

            Console.WriteLine("Getting similar documents from vector db...");
            var lastMessage = _memory.ChatHistory.Messages.LastOrDefault();
            //var searchInput = (lastMessage.Content ?? " ") + "\n" + question; // adding the last message to the search input
            var searchInput = question;
            Console.WriteLine("Search Input : " + searchInput);
            var similarDocuments = await vectorCollection.GetSimilarDocuments(
                _embeddingModel,
                question,
                amount: 5,
                VectorSearchType.SimilarityScoreThreshold,
                0.8f
            );

            Console.WriteLine("\nSimilar Documents : \n" + similarDocuments.AsString() + "\n");

            Console.WriteLine("\nResponding...");
            // Build a new chain by prepending the user's input to the original chain
            var currentChain = Chain.Set(question, "input")
                | Chain.Set(similarDocuments.AsString(), "context")
                | _chain;

            var answers = await currentChain.RunAsync("text").ConfigureAwait(false);

            // Remove Markdown bold formatting (**)
            answers = CleanResponse(answers);

            // TODO : Implement as streaming message
            _chatModel.DeltaReceived -= eventHandler;

            return answers;
        }


        public async Task<string> ChatHistoryAsync(ChatHistory history, EventHandler<ChatResponseDelta> eventHandler)
        {
            _chatModel.DeltaReceived += eventHandler;

            Console.WriteLine("Chatting with the AI with history input...");

            var userInput = history.Messages.Last(); // input by user
            history.Messages.RemoveAt(history.Messages.Count - 1); // remove the last message from the history
            if (userInput.Role != RoleConstants.Human)
                throw new Exception("Last Message must from Human");
            var userQues = userInput.Content;

            // Build history message from template
            Console.WriteLine("Building history message...");
            var historyString = new StringBuilder();
            var latest3Message = history.Messages.Skip(Math.Max(0, history.Messages.Count - 3));
            foreach (var message in history.Messages)
            {
                //check the role must be Human or AI in RoleConstants
                if (message.Role != RoleConstants.Human && message.Role != RoleConstants.AI)
                {
                    throw new Exception("All Role must be Human or AI");
                }

                if (message == history.Messages.Last())
                {
                    historyString.Append($"{message.Role}: {message.Content}");
                }
                else
                {
                    historyString.AppendLine($"{message.Role}: {message.Content}");
                }
            }
            Console.WriteLine(historyString);

            // TODO : Get knowledge from the vector db
            Console.WriteLine("Attempt to Getting vector collection...");
            var vectorCollection = await _vectorDatabase.GetOrCreateCollectionAsync("focs_clean", dimensions: 768);
            if (vectorCollection.IsEmptyAsync().Result)
            {
                Console.WriteLine("Vector Collections for focs is empty....");
                //await VectorDbUtils.DownloadWebsiteHMTLToVectorDB(_vectorDatabase, _embeddingModel);
                await VectorDbUtils.LoadTextFilesToVectorDB(_vectorDatabase, _embeddingModel);
            }
            Console.WriteLine("Getting similar documents / knowledge from vector db...");
            var knowledge = await vectorCollection.GetSimilarDocuments(
                _embeddingModel,
                userQues,
                amount: 5,
                VectorSearchType.SimilarityScoreThreshold,
                0.8f
            );
            Console.WriteLine("\nKnowledge : \n" + knowledge.AsString() + "\n");

            Console.WriteLine("Responding...");
            var llmInput = PopulateTemplate(knowledge.AsString(), historyString.ToString(), userQues);

            Console.WriteLine("llmInput : " + llmInput);

            Console.WriteLine("Streaming responses...");
            var response = await _chatModel.GenerateAsync(llmInput);

            _chatModel.DeltaReceived -= eventHandler;

            return response;
        }

        private BaseChatMemory GetChatMemory()
        {
            // The memory will add prefixes to messages to indicate where they came from
            // The prefixes specified here should match those used in our prompt template
            MessageFormatter messageFormatter = new MessageFormatter
            {
                AiPrefix = "AI",
                HumanPrefix = "Human"
            };

            BaseChatMessageHistory chatHistory = new ChatMessageHistory();

            //return GetConversationBufferMemory(chatHistory, messageFormatter);
            return GetConversationWindowBufferMemory(chatHistory, messageFormatter);
        }

        private BaseChatMemory GetConversationWindowBufferMemory(BaseChatMessageHistory chatHistory, MessageFormatter messageFormatter)
        {
            return new ConversationWindowBufferMemory(chatHistory)
            {
                WindowSize = 3,
                Formatter = messageFormatter
            };
        }

        private string PopulateTemplate(string context, string history, string input)
        {
            return _template.Replace("{context}", context)
                            .Replace("{history}", history)
                            .Replace("{input}", input);
        }

    }
}
