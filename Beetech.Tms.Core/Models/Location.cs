using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class Location : BaseEntity
{

    [Required, MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(200)]
    public string? Description { get; set; }

    public LocationType Type { get; set; }

    public bool IsActive { get; set; } = true;

    // Navigation
    public ICollection<TextileItem> Items { get; set; } = new List<TextileItem>();
}
