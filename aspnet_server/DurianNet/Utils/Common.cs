namespace DurianNet.Utils
{
    public static class Common
    {

        public static string GetBase64Image(string imagePath)
        {
            if (System.IO.File.Exists(imagePath))
            {
                var imageBytes = System.IO.File.ReadAllBytes(imagePath);
                return Convert.ToBase64String(imageBytes);
            }

            return string.Empty;
        }

    }
}
