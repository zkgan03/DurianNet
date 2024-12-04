using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace DurianNet.Migrations
{
    /// <inheritdoc />
    public partial class AddPasswordResetTokenColumns : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "1d48babd-765a-4cfa-a106-5f625a9976b6");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "acfe71b1-1fe4-4a65-8826-554131a6a167");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "3018b1c3-29ba-4cdf-8a35-20ea2c8e719a", null, "Admin", "ADMIN" },
                    { "a27f589a-6542-46c0-8f5e-c6f2c56bd30b", null, "User", "USER" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
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
                    { "1d48babd-765a-4cfa-a106-5f625a9976b6", null, "User", "USER" },
                    { "acfe71b1-1fe4-4a65-8826-554131a6a167", null, "Admin", "ADMIN" }
                });
        }
    }
}
