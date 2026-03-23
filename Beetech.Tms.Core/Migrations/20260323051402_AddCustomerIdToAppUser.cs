using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Beetech.Tms.Core.Migrations
{
    /// <inheritdoc />
    public partial class AddCustomerIdToAppUser : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "CustomerId",
                table: "Users",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "CustomerId",
                table: "Transactions",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Description",
                table: "Transactions",
                type: "TEXT",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "SourceTransactionId",
                table: "Transactions",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "TargetQuantity",
                table: "Transactions",
                type: "INTEGER",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<string>(
                name: "Notes",
                table: "TransactionItems",
                type: "TEXT",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "PackingUnitId",
                table: "TransactionItems",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "CurrentCustomerId",
                table: "TextileItems",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.CreateTable(
                name: "Customers",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Name = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    Code = table.Column<string>(type: "TEXT", maxLength: 50, nullable: true),
                    IsInternal = table.Column<bool>(type: "INTEGER", nullable: false),
                    Address = table.Column<string>(type: "TEXT", maxLength: 200, nullable: true),
                    ContactPerson = table.Column<string>(type: "TEXT", maxLength: 100, nullable: true),
                    ContactPhone = table.Column<string>(type: "TEXT", maxLength: 50, nullable: true),
                    IsActive = table.Column<bool>(type: "INTEGER", nullable: false),
                    IsDeleted = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "TEXT", nullable: true),
                    DeletedAt = table.Column<DateTime>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Customers", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "InventoryAuditSessions",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    LocationId = table.Column<int>(type: "INTEGER", nullable: false),
                    StartTime = table.Column<DateTime>(type: "TEXT", nullable: false),
                    EndTime = table.Column<DateTime>(type: "TEXT", nullable: true),
                    Status = table.Column<string>(type: "TEXT", nullable: false),
                    PerformByName = table.Column<string>(type: "TEXT", nullable: false),
                    IsDeleted = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "TEXT", nullable: true),
                    DeletedAt = table.Column<DateTime>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_InventoryAuditSessions", x => x.Id);
                    table.ForeignKey(
                        name: "FK_InventoryAuditSessions_Locations_LocationId",
                        column: x => x.LocationId,
                        principalTable: "Locations",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "PackingUnits",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Code = table.Column<string>(type: "TEXT", maxLength: 50, nullable: false),
                    Type = table.Column<int>(type: "INTEGER", nullable: false),
                    TransactionId = table.Column<int>(type: "INTEGER", nullable: false),
                    Weight = table.Column<decimal>(type: "TEXT", nullable: true),
                    IsDeleted = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "TEXT", nullable: true),
                    DeletedAt = table.Column<DateTime>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_PackingUnits", x => x.Id);
                    table.ForeignKey(
                        name: "FK_PackingUnits_Transactions_TransactionId",
                        column: x => x.TransactionId,
                        principalTable: "Transactions",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "InventoryAuditResults",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    InventoryAuditSessionId = table.Column<int>(type: "INTEGER", nullable: false),
                    TextileItemId = table.Column<int>(type: "INTEGER", nullable: true),
                    Tag = table.Column<string>(type: "TEXT", nullable: true),
                    AssetName = table.Column<string>(type: "TEXT", nullable: true),
                    Status = table.Column<string>(type: "TEXT", nullable: false),
                    IsValid = table.Column<bool>(type: "INTEGER", nullable: false),
                    ScanAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    IsDeleted = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "TEXT", nullable: true),
                    DeletedAt = table.Column<DateTime>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_InventoryAuditResults", x => x.Id);
                    table.ForeignKey(
                        name: "FK_InventoryAuditResults_InventoryAuditSessions_InventoryAuditSessionId",
                        column: x => x.InventoryAuditSessionId,
                        principalTable: "InventoryAuditSessions",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_InventoryAuditResults_TextileItems_TextileItemId",
                        column: x => x.TextileItemId,
                        principalTable: "TextileItems",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateIndex(
                name: "IX_Users_CustomerId",
                table: "Users",
                column: "CustomerId");

            migrationBuilder.CreateIndex(
                name: "IX_Transactions_CustomerId",
                table: "Transactions",
                column: "CustomerId");

            migrationBuilder.CreateIndex(
                name: "IX_Transactions_SourceTransactionId",
                table: "Transactions",
                column: "SourceTransactionId");

            migrationBuilder.CreateIndex(
                name: "IX_TransactionItems_PackingUnitId",
                table: "TransactionItems",
                column: "PackingUnitId");

            migrationBuilder.CreateIndex(
                name: "IX_TextileItems_CurrentCustomerId",
                table: "TextileItems",
                column: "CurrentCustomerId");

            migrationBuilder.CreateIndex(
                name: "IX_InventoryAuditResults_InventoryAuditSessionId",
                table: "InventoryAuditResults",
                column: "InventoryAuditSessionId");

            migrationBuilder.CreateIndex(
                name: "IX_InventoryAuditResults_TextileItemId",
                table: "InventoryAuditResults",
                column: "TextileItemId");

            migrationBuilder.CreateIndex(
                name: "IX_InventoryAuditSessions_LocationId",
                table: "InventoryAuditSessions",
                column: "LocationId");

            migrationBuilder.CreateIndex(
                name: "IX_PackingUnits_TransactionId",
                table: "PackingUnits",
                column: "TransactionId");

            migrationBuilder.AddForeignKey(
                name: "FK_TextileItems_Customers_CurrentCustomerId",
                table: "TextileItems",
                column: "CurrentCustomerId",
                principalTable: "Customers",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_TransactionItems_PackingUnits_PackingUnitId",
                table: "TransactionItems",
                column: "PackingUnitId",
                principalTable: "PackingUnits",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_Transactions_Customers_CustomerId",
                table: "Transactions",
                column: "CustomerId",
                principalTable: "Customers",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_Transactions_Transactions_SourceTransactionId",
                table: "Transactions",
                column: "SourceTransactionId",
                principalTable: "Transactions",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_Users_Customers_CustomerId",
                table: "Users",
                column: "CustomerId",
                principalTable: "Customers",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_TextileItems_Customers_CurrentCustomerId",
                table: "TextileItems");

            migrationBuilder.DropForeignKey(
                name: "FK_TransactionItems_PackingUnits_PackingUnitId",
                table: "TransactionItems");

            migrationBuilder.DropForeignKey(
                name: "FK_Transactions_Customers_CustomerId",
                table: "Transactions");

            migrationBuilder.DropForeignKey(
                name: "FK_Transactions_Transactions_SourceTransactionId",
                table: "Transactions");

            migrationBuilder.DropForeignKey(
                name: "FK_Users_Customers_CustomerId",
                table: "Users");

            migrationBuilder.DropTable(
                name: "Customers");

            migrationBuilder.DropTable(
                name: "InventoryAuditResults");

            migrationBuilder.DropTable(
                name: "PackingUnits");

            migrationBuilder.DropTable(
                name: "InventoryAuditSessions");

            migrationBuilder.DropIndex(
                name: "IX_Users_CustomerId",
                table: "Users");

            migrationBuilder.DropIndex(
                name: "IX_Transactions_CustomerId",
                table: "Transactions");

            migrationBuilder.DropIndex(
                name: "IX_Transactions_SourceTransactionId",
                table: "Transactions");

            migrationBuilder.DropIndex(
                name: "IX_TransactionItems_PackingUnitId",
                table: "TransactionItems");

            migrationBuilder.DropIndex(
                name: "IX_TextileItems_CurrentCustomerId",
                table: "TextileItems");

            migrationBuilder.DropColumn(
                name: "CustomerId",
                table: "Users");

            migrationBuilder.DropColumn(
                name: "CustomerId",
                table: "Transactions");

            migrationBuilder.DropColumn(
                name: "Description",
                table: "Transactions");

            migrationBuilder.DropColumn(
                name: "SourceTransactionId",
                table: "Transactions");

            migrationBuilder.DropColumn(
                name: "TargetQuantity",
                table: "Transactions");

            migrationBuilder.DropColumn(
                name: "Notes",
                table: "TransactionItems");

            migrationBuilder.DropColumn(
                name: "PackingUnitId",
                table: "TransactionItems");

            migrationBuilder.DropColumn(
                name: "CurrentCustomerId",
                table: "TextileItems");
        }
    }
}
