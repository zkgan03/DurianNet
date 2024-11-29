using System.Net;
using System.Net.Mail;

namespace DurianNet.Services.EmailService
{
    public static class EmailService
    {
        private const string SenderEmail = "magnetic636@gmail.com"; // Update to your email
        private const string SenderPassword = "tqbp zars jlzu wpof"; // Update to your email password or app password
        private const string UserResetLink = "http://duriannet.com/reset-password"; // Update to your app's reset link
        private const string AdminResetLink = "http://localhost:5176/account/ResetPassword"; // Update to your admin web reset link 

        public static void SendPasswordRecoveryEmail(string receiverEmail, bool isAdmin)
        {
            try
            {
                string resetLink = isAdmin ? AdminResetLink : UserResetLink;

                MailMessage mailMessage = new MailMessage(SenderEmail, receiverEmail)
                {
                    Subject = "Password Recovery from DurianNet",
                    Body = $@"
                <h3>Reset your password</h3>
                <p>Click the link below to reset your password:</p>
                <a href='{resetLink}' style='color:white;border:1px solid black;background-color:black;padding: 15px 10px;'>
                    Reset Password
                </a>",
                    IsBodyHtml = true
                };

                SmtpClient smtpClient = new SmtpClient("smtp.gmail.com", 587)
                {
                    EnableSsl = true,
                    UseDefaultCredentials = false,
                    Credentials = new NetworkCredential(SenderEmail, SenderPassword)
                };

                smtpClient.Send(mailMessage);
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException($"Failed to send email: {ex.Message}");
            }
        }

        public static void SendOtpEmail(string receiverEmail, string otp)
        {
            try
            {
                string subject = "Your OTP Code for DurianNet";
                string body = $@"
            <h3>One-Time Password (OTP)</h3>
            <p>Use the code below to reset your password:</p>
            <h1 style='color:blue;'>{otp}</h1>
            <p>This code will expire in 10 minutes.</p>";

                MailMessage mailMessage = new MailMessage(SenderEmail, receiverEmail)
                {
                    Subject = subject,
                    Body = body,
                    IsBodyHtml = true
                };

                SmtpClient smtpClient = new SmtpClient("smtp.gmail.com", 587)
                {
                    EnableSsl = true,
                    UseDefaultCredentials = false,
                    Credentials = new NetworkCredential(SenderEmail, SenderPassword)
                };

                smtpClient.Send(mailMessage);
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException($"Failed to send email: {ex.Message}");
            }
        }



    }
}
