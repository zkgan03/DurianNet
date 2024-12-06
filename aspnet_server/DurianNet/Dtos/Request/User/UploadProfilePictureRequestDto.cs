namespace DurianNet.Dtos.Request.User
{
    public class UploadProfilePictureRequestDto
    {
        public string FileName { get; set; }
        public string FileContent { get; set; } // Base64-encoded file content
    }

}
