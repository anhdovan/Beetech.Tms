using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Beetech.Tms.Core.Utils;

namespace Beetech.Tms.Core.Models;

public class TextileItem : BaseEntity
{
    /// <summary>
    /// Unique 24 hex digits used for file RFID EPC.
    /// Calculated from ID using ChaCha20 encryption.
    /// </summary>
    [NotMapped]
    public string Code => Id > 0
        ? ChaCha20Util.EncryptExt(Id, RfidTagConstants.AssetTag)
        : string.Empty;

    public int CategoryId { get; set; }

    public ItemStatus Status { get; set; } = ItemStatus.Available;

    public int WashCount { get; set; } = 0;

    public int? CurrentLocationId { get; set; }
    public int? CurrentDepartmentId { get; set; }

    public DateTime LastScanAt { get; set; } = DateTime.UtcNow;

    // Navigation
    [ForeignKey(nameof(CategoryId))]
    public Category Category { get; set; } = null!;

    [ForeignKey(nameof(CurrentLocationId))]
    public Location? CurrentLocation { get; set; }

    [ForeignKey(nameof(CurrentDepartmentId))]
    public Department? CurrentDepartment { get; set; }
}
