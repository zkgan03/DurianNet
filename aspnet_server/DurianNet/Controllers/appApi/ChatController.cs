/*using Azure;
using Microsoft.AspNetCore.Mvc;
using System.Text.Json;
using LangChain.Providers;
using DurianNet.Dtos;
using DurianNet.Services.Chatbot;

namespace DurianNet.Controllers.appApi
{
    [Route("appApi/chatbot")]
    [ApiController]
    public class ChatController : ControllerBase
    {
        private readonly IChatbotService _chatbot;

        public ChatController(IChatbotService chatbotService)
        {
            _chatbot = chatbotService;
        }

        [HttpPost("WithHistory")]
        public async Task ChatWithHistory([FromBody] ChatHistory chatHistory, CancellationToken cancellationToken)
        {
            var requesterAddress = HttpContext.Connection.RemoteIpAddress?.ToString();
            var requesterPort = HttpContext.Connection.RemotePort;
            Console.WriteLine($"Request address: {requesterAddress}, port: {requesterPort}");

            // If chatHistory is null or empty, return BadRequest
            if (chatHistory.Messages == null || !chatHistory.Messages.Any())
            {
                Console.WriteLine("Please provide at least 1 Human Question");
                Response.StatusCode = 400;
                await Response.WriteAsJsonAsync(new
                {
                    error = "Please provide at least 1 Human Question"
                });
                return;
            }

            // If the last message is not from the user, return BadRequest
            if (chatHistory.Messages.Last().Role != RoleConstants.Human)
            {
                Console.WriteLine("Last Message Must Be from Human");
                Response.StatusCode = 400;
                await Response.WriteAsJsonAsync(new
                {
                    error = "Last Message Must Be from Human"
                });
                return;
            }

            Response.ContentType = "text/event-stream";

            EventHandler<ChatResponseDelta> chatbotResHandler = async (sender, e) =>
            {
                if (!string.IsNullOrEmpty(e.Content))
                {
                    // Directly write the content without wrapping it in `data: {"data": ""}`
                    await Response.WriteAsync(e.Content);
                    await Response.Body.FlushAsync();
                }
            };

            // Call the chatbot service to process the chat history
            try
            {
                await _chatbot.ChatHistoryAsync(chatHistory, chatbotResHandler);
                await Response.CompleteAsync();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error: {ex.Message}");
                Response.StatusCode = 500;
                await Response.WriteAsJsonAsync(new
                {
                    error = "An error occurred while processing your request.",
                    details = ex.Message
                });
            }
        }
    }
}*/


using Azure;
using Microsoft.AspNetCore.Mvc;
using System.Text.Json;
using LangChain.Providers;
using DurianNet.Services.Chatbot;
using DurianNet.Dtos.Request.Chat;

namespace DurianNet.Controllers.appApi
{
    [Route("appApi/chatbot")]
    [ApiController]
    public class ChatController : ControllerBase
    {
        private readonly IChatbotService _chatbot;

        public ChatController(IChatbotService chatbotService)
        {
            _chatbot = chatbotService;
        }

        [HttpPost("WithHistory")]
        public async Task ChatWithHistory([FromBody] ChatHistory chatHistory, CancellationToken cancellationToken)
        {
            var requesterAddress = HttpContext.Connection.RemoteIpAddress?.ToString();
            var requesterPort = HttpContext.Connection.RemotePort;
            Console.WriteLine($"request address: {requesterAddress}, port: {requesterPort}");

            //if chatHistory is null, return BadRequest
            if (chatHistory.Messages.Count == 0)
            {
                Console.WriteLine("Please provide at least 1 Human Question");
                Response.StatusCode = 400;
                await Response.WriteAsJsonAsync(new
                {
                    error = "Please provide at least 1 Human Question"
                });
                return;
            }

            // if the last message is not from the user, return BadRequest
            if (chatHistory.Messages.Last().Role != RoleConstants.Human)
            {
                Console.WriteLine("Last Message Must from Human");
                Response.StatusCode = 400;
                //await Response.WriteAsync("Last Message Must from Human");
                await Response.WriteAsJsonAsync(new
                {
                    error = "Last Message Must from Human"
                });
                return;
            }

            Response.ContentType = "text/event-stream";

            EventHandler<ChatResponseDelta> chatbotResHandler = async (sender, e) =>
            {
                //Console.Write(e.Content);
                var json = $"data: {JsonSerializer.Serialize(new { data = e.Content })}\n\n"; // serialize the response as string, because SSE only accepts string
                //var data = $"data: {e.Content}\n\n";
                Console.Write(json);
                await Response.WriteAsync(json);
                await Response.Body.FlushAsync();
            };

            await _chatbot.ChatHistoryAsync(chatHistory, chatbotResHandler);

            await Response.CompleteAsync();

        }

    }
}
