namespace DurianNet.Dtos.Request.Chat
{
    public class ChatHistory
    {
        public List<Message> Messages { get; set; } = [];

        public class Message
        {
            public string Role { get; set; } = RoleConstants.Human;
            public string Content { get; set; } = "";
        }
    }

    public static class RoleConstants
    {
        public const string Human = "Human";
        public const string AI = "AI";
    }
}
