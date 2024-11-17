using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace DurianNet.Migrations
{
    /// <inheritdoc />
    public partial class FavoriteDurianManyToMany : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfiles_DurianVideos_DurianVideoId",
                table: "DurianProfiles");

            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfileSeller_DurianProfiles_DurianProfilesDurianId",
                table: "DurianProfileSeller");

            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfileUser_DurianProfiles_FavoriteDurianDurianId",
                table: "DurianProfileUser");

            migrationBuilder.DropPrimaryKey(
                name: "PK_DurianProfiles",
                table: "DurianProfiles");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "5fcc3f45-60c4-4799-a6a9-956cf3f3a51a");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "e9cadf98-1417-42ba-a2d2-baac1821f345");

            migrationBuilder.RenameTable(
                name: "DurianProfiles",
                newName: "DurianProfile");

            migrationBuilder.RenameIndex(
                name: "IX_DurianProfiles_DurianVideoId",
                table: "DurianProfile",
                newName: "IX_DurianProfile_DurianVideoId");

            migrationBuilder.AddPrimaryKey(
                name: "PK_DurianProfile",
                table: "DurianProfile",
                column: "DurianId");

            migrationBuilder.CreateTable(
                name: "FavoriteDurian",
                columns: table => new
                {
                    UserId = table.Column<string>(type: "nvarchar(450)", nullable: false),
                    DurianId = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FavoriteDurian", x => new { x.UserId, x.DurianId });
                    table.ForeignKey(
                        name: "FK_FavoriteDurian_AspNetUsers_UserId",
                        column: x => x.UserId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_FavoriteDurian_DurianProfile_DurianId",
                        column: x => x.DurianId,
                        principalTable: "DurianProfile",
                        principalColumn: "DurianId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "2e6eb08c-4490-4ab2-a68e-71b75b5dea5a", null, "Admin", "ADMIN" },
                    { "4775cec9-0abc-4675-8fdb-ebce36ce1d8f", null, "User", "USER" }
                });

            migrationBuilder.CreateIndex(
                name: "IX_FavoriteDurian_DurianId",
                table: "FavoriteDurian",
                column: "DurianId");

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfile_DurianVideos_DurianVideoId",
                table: "DurianProfile",
                column: "DurianVideoId",
                principalTable: "DurianVideos",
                principalColumn: "VideoId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfileSeller_DurianProfile_DurianProfilesDurianId",
                table: "DurianProfileSeller",
                column: "DurianProfilesDurianId",
                principalTable: "DurianProfile",
                principalColumn: "DurianId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfileUser_DurianProfile_FavoriteDurianDurianId",
                table: "DurianProfileUser",
                column: "FavoriteDurianDurianId",
                principalTable: "DurianProfile",
                principalColumn: "DurianId",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfile_DurianVideos_DurianVideoId",
                table: "DurianProfile");

            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfileSeller_DurianProfile_DurianProfilesDurianId",
                table: "DurianProfileSeller");

            migrationBuilder.DropForeignKey(
                name: "FK_DurianProfileUser_DurianProfile_FavoriteDurianDurianId",
                table: "DurianProfileUser");

            migrationBuilder.DropTable(
                name: "FavoriteDurian");

            migrationBuilder.DropPrimaryKey(
                name: "PK_DurianProfile",
                table: "DurianProfile");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "2e6eb08c-4490-4ab2-a68e-71b75b5dea5a");

            migrationBuilder.DeleteData(
                table: "AspNetRoles",
                keyColumn: "Id",
                keyValue: "4775cec9-0abc-4675-8fdb-ebce36ce1d8f");

            migrationBuilder.RenameTable(
                name: "DurianProfile",
                newName: "DurianProfiles");

            migrationBuilder.RenameIndex(
                name: "IX_DurianProfile_DurianVideoId",
                table: "DurianProfiles",
                newName: "IX_DurianProfiles_DurianVideoId");

            migrationBuilder.AddPrimaryKey(
                name: "PK_DurianProfiles",
                table: "DurianProfiles",
                column: "DurianId");

            migrationBuilder.InsertData(
                table: "AspNetRoles",
                columns: new[] { "Id", "ConcurrencyStamp", "Name", "NormalizedName" },
                values: new object[,]
                {
                    { "5fcc3f45-60c4-4799-a6a9-956cf3f3a51a", null, "Admin", "ADMIN" },
                    { "e9cadf98-1417-42ba-a2d2-baac1821f345", null, "User", "USER" }
                });

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfiles_DurianVideos_DurianVideoId",
                table: "DurianProfiles",
                column: "DurianVideoId",
                principalTable: "DurianVideos",
                principalColumn: "VideoId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfileSeller_DurianProfiles_DurianProfilesDurianId",
                table: "DurianProfileSeller",
                column: "DurianProfilesDurianId",
                principalTable: "DurianProfiles",
                principalColumn: "DurianId",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_DurianProfileUser_DurianProfiles_FavoriteDurianDurianId",
                table: "DurianProfileUser",
                column: "FavoriteDurianDurianId",
                principalTable: "DurianProfiles",
                principalColumn: "DurianId",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
