using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace DurianNet.Migrations
{
    /// <inheritdoc />
    public partial class FinalTesting : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "5fe3d208-0c8d-476e-a8f5-b2ebf220b138");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "8341d3c1-c0d2-452c-8ff3-ab2007b5d026");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "1c984a2c-5f62-4c87-861b-17127eed5b9f", null, "User", "USER" },
                    { "70a24d04-4b45-4062-b99d-cc510c18439c", null, "Admin", "ADMIN" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "1c984a2c-5f62-4c87-861b-17127eed5b9f");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "70a24d04-4b45-4062-b99d-cc510c18439c");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "5fe3d208-0c8d-476e-a8f5-b2ebf220b138", null, "Admin", "ADMIN" },
                    { "8341d3c1-c0d2-452c-8ff3-ab2007b5d026", null, "User", "USER" }
                });
        }
    }
}
