
using Microsoft.EntityFrameworkCore.Storage.ValueConversion.Internal;

namespace DurianNet.Utils
{
    public class ImageUtils
    {
        public static string GetBase64Image(string imagePath)
        {
            if (File.Exists(imagePath))
            {
                var imageBytes = File.ReadAllBytes(imagePath);
                return Convert.ToBase64String(imageBytes);
            }

            return string.Empty;
        }

        public static string GetBase64Image(byte[] imageBytes)
        {
            return Convert.ToBase64String(imageBytes);
        }

        public static byte[] GetImageBytes(string base64Image)
        {
            return Convert.FromBase64String(base64Image);
        }

        public static void SaveImage(string base64Image, string imagePath)
        {
            var imageBytes = Convert.FromBase64String(base64Image);
            File.WriteAllBytes(imagePath, imageBytes);
        }

        public static void SaveImage(byte[] imageBytes, string imagePath)
        {
            File.WriteAllBytes(imagePath, imageBytes);
        }

    }
}
