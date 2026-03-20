using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class Transaction : BaseEntity
{

    [Required]
    [MaxLength(50)]
    public string TransactionNumber { get; set; } = string.Empty;

    public TransactionType Type { get; set; }

    public int? FromLocationId { get; set; }
    public Location? FromLocation { get; set; }

    public int? ToLocationId { get; set; }
    public Location? ToLocation { get; set; }

    public int? DepartmentId { get; set; }
    public Department? Department { get; set; }

    public DateTime TransactionDate { get; set; } = DateTime.UtcNow;

    public int? CreatedById { get; set; }
    public AppUser? CreatedBy { get; set; }

    public string? Notes { get; set; }

    public ICollection<TransactionItem> Items { get; set; } = new List<TransactionItem>();
}
