using System.ComponentModel.DataAnnotations.Schema;

namespace Beetech.Tms.Core.Models;

public class TransactionItem : BaseEntity
{

    public int TransactionId { get; set; }
    public Transaction? Transaction { get; set; }

    public int TextileItemId { get; set; }
    public TextileItem? TextileItem { get; set; }

    public int? PackingUnitId { get; set; }
    public PackingUnit? PackingUnit { get; set; }

    public ItemStatus StatusAtTransaction { get; set; }

    public string? Notes { get; set; }
}
