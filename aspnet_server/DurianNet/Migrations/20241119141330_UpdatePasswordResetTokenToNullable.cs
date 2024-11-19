using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace DurianNet.Migrations
{
    /// <inheritdoc />
    public partial class UpdatePasswordResetTokenToNullable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Alter the PasswordResetToken column to allow null values
            migrationBuilder.AlterColumn<string>(
                name: "PasswordResetToken",
                table: "AspNetUsers",
                type: "nvarchar(max)",
                nullable: true,
                oldClrType: typeof(string),
                oldType: "nvarchar(max)");

            // Alter the ResetTokenExpires column to allow null values (if required)
            migrationBuilder.AlterColumn<DateTime?>(
                name: "ResetTokenExpires",
                table: "AspNetUsers",
                type: "datetime2",
                nullable: true,
                oldClrType: typeof(DateTime),
                oldType: "datetime2");

            // Update role seed data (if applicable)
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "3018b1c3-29ba-4cdf-8a35-20ea2c8e719a");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "a27f589a-6542-46c0-8f5e-c6f2c56bd30b");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "9d3a57f8-c36c-4190-a666-01c27aa19afe", null, "Admin", "ADMIN" },
                    { "b9a506bb-b989-4966-ad4c-e8628198d729", null, "User", "USER" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            // Revert the PasswordResetToken column to NOT NULL
            migrationBuilder.AlterColumn<string>(
                name: "PasswordResetToken",
                table: "AspNetUsers",
                type: "nvarchar(max)",
                nullable: false,
                oldClrType: typeof(string),
                oldType: "nvarchar(max)");

            // Revert the ResetTokenExpires column to NOT NULL
            migrationBuilder.AlterColumn<DateTime>(
                name: "ResetTokenExpires",
                table: "AspNetUsers",
                type: "datetime2",
                nullable: false,
                oldClrType: typeof(DateTime),
                oldType: "datetime2");

            // Revert role seed data (if applicable)
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "9d3a57f8-c36c-4190-a666-01c27aa19afe");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "b9a506bb-b989-4966-ad4c-e8628198d729");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "3018b1c3-29ba-4cdf-8a35-20ea2c8e719a", null, "Admin", "ADMIN" },
                    { "a27f589a-6542-46c0-8f5e-c6f2c56bd30b", null, "User", "USER" }
                });
        }
    }
}
