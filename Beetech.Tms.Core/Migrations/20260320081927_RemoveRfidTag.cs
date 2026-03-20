using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Beetech.Tms.Core.Migrations
{
    /// <inheritdoc />
    public partial class RemoveRfidTag : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_TextileItems_RfidTag",
                table: "TextileItems");

            migrationBuilder.DropColumn(
                name: "RfidTag",
                table: "TextileItems");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "RfidTag",
                table: "TextileItems",
                type: "TEXT",
                maxLength: 50,
                nullable: false,
                defaultValue: "");

            migrationBuilder.CreateIndex(
                name: "IX_TextileItems_RfidTag",
                table: "TextileItems",
                column: "RfidTag",
                unique: true);
        }
    }
}
