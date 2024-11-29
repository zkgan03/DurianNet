using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace DurianNet.Migrations
{
    /// <inheritdoc />
    public partial class AddOTPFieldsToUser : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "1279d561-39c7-4ea0-9ace-5e6af0e597c7");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "cbfc8d64-97d0-4de7-a9c1-bcd37eef0858");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "8b04291f-ced4-4238-acad-d607228d34cd", null, "User", "USER" },
                    { "e3b0e49a-93d0-4d5d-84e8-b089d1c67b77", null, "Admin", "ADMIN" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "8b04291f-ced4-4238-acad-d607228d34cd");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "e3b0e49a-93d0-4d5d-84e8-b089d1c67b77");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "1279d561-39c7-4ea0-9ace-5e6af0e597c7", null, "User", "USER" },
                    { "cbfc8d64-97d0-4de7-a9c1-bcd37eef0858", null, "Admin", "ADMIN" }
                });
        }
    }
}
