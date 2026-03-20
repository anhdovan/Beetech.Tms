using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class Department : BaseEntity
{

    [Required, MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    public bool IsActive { get; set; } = true;

    // Navigation
    public ICollection<AppUser> Users { get; set; } = new List<AppUser>();
}
