using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class PackingUnit : BaseEntity
{
    [Required]
    [MaxLength(50)]
    public string Code { get; set; } = string.Empty;

    public PackageType Type { get; set; }

    public int TransactionId { get; set; }
    public Transaction? Transaction { get; set; }

    public decimal? Weight { get; set; }

    public ICollection<TransactionItem> Items { get; set; } = new List<TransactionItem>();
}
