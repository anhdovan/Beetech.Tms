using System;
using System.Collections.Generic;

namespace Beetech.Tms.Core.Models
{
    public class InventoryAuditSession : BaseEntity
    {
        public int LocationId { get; set; }
        public Location? Location { get; set; }
        public DateTime StartTime { get; set; } = DateTime.UtcNow;
        public DateTime? EndTime { get; set; }
        public string Status { get; set; } = "In Progress"; // In Progress, Completed
        public string PerformByName { get; set; } = string.Empty;
        public List<InventoryAuditResult> Results { get; set; } = new();
    }

    public class InventoryAuditResult : BaseEntity
    {
        public int InventoryAuditSessionId { get; set; }
        public InventoryAuditSession? InventoryAuditSession { get; set; }
        public int? TextileItemId { get; set; }
        public TextileItem? TextileItem { get; set; }
        public string? Tag { get; set; }
        public string? AssetName { get; set; }
        public string Status { get; set; } = string.Empty; // Verified, Missing, Relocated, Unknown
        public bool IsValid { get; set; }
        public DateTime ScanAt { get; set; } = DateTime.UtcNow;
    }
}
