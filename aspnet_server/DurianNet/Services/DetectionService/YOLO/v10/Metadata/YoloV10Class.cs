namespace DurianNet.Services.DetectionService.YOLO.v10.Metadata
{
    public class YoloV10Class(int id, string name)
    {
        public int Id { get; } = id;

        public string Name { get; } = name;

        public override string ToString()
        {
            return $"{Id}: '{Name}'";
        }
    }
}
