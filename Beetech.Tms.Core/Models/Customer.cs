using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class Customer : BaseEntity
{
    [Required]
    [MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(50)]
    public string? Code { get; set; }

    public bool IsInternal { get; set; } = true;

    [MaxLength(200)]
    public string? Address { get; set; }

    [MaxLength(100)]
    public string? ContactPerson { get; set; }

    [MaxLength(50)]
    public string? ContactPhone { get; set; }

    public bool IsActive { get; set; } = true;

    public ICollection<TextileItem> Items { get; set; } = new List<TextileItem>();
    public ICollection<Transaction> Transactions { get; set; } = new List<Transaction>();
}
