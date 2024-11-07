using MessagePack;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace DurianNet.Hubs.Dto
{
    [MessagePackObject]
    public class ConfigurationRequest
    {
        [Key(0)]
        public float ConfidenceThreshold { get; set; }
        [Key(1)]
        public float IoUThreshold { get; set; }
        [Key(2)]
        public int MaxNumberDetection { get; set; }

    }
}
