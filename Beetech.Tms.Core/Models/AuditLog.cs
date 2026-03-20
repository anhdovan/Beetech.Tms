using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Beetech.Tms.Core.Models;

public class AuditLog
{
    public int Id { get; set; }

    public DateTime Timestamp { get; set; } = DateTime.UtcNow;

    public int? UserId { get; set; }

    [MaxLength(50)]
    public string EntityName { get; set; } = string.Empty;

    public int EntityId { get; set; }

    public AuditAction Action { get; set; }

    [MaxLength(500)]
    public string? Details { get; set; }

    [MaxLength(50)]
    public string? IpAddress { get; set; }

    // Navigation
    [ForeignKey(nameof(UserId))]
    public AppUser? User { get; set; }
}
