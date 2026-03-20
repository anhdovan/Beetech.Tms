using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Beetech.Tms.Core.Migrations
{
    /// <inheritdoc />
    public partial class AddDepartmentToItem : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "CurrentDepartmentId",
                table: "TextileItems",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_TextileItems_CurrentDepartmentId",
                table: "TextileItems",
                column: "CurrentDepartmentId");

            migrationBuilder.AddForeignKey(
                name: "FK_TextileItems_Departments_CurrentDepartmentId",
                table: "TextileItems",
                column: "CurrentDepartmentId",
                principalTable: "Departments",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_TextileItems_Departments_CurrentDepartmentId",
                table: "TextileItems");

            migrationBuilder.DropIndex(
                name: "IX_TextileItems_CurrentDepartmentId",
                table: "TextileItems");

            migrationBuilder.DropColumn(
                name: "CurrentDepartmentId",
                table: "TextileItems");
        }
    }
}
