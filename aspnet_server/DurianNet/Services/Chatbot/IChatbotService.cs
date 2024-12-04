using DurianNet.Dtos.Request.Chat;
using LangChain.Providers;

namespace DurianNet.Services.Chatbot
{
    public interface IChatbotService
    {
        Task<string> ChatAsync(string question, EventHandler<ChatResponseDelta> eventHandler);

        Task<string> ChatHistoryAsync(ChatHistory history, EventHandler<ChatResponseDelta> eventHandler);
    }
}
