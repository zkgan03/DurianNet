using System.Runtime.Serialization;

namespace DurianNet.Exceptions
{
    public class DataNotFoundException : Exception
    {
        public DataNotFoundException()
        {
        }
        public DataNotFoundException(string message) : base(message)
        {
        }
        public DataNotFoundException(string? message, Exception? innerException) : base(message, innerException)
        {
        }
    }
}
