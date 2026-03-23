using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Beetech.Tms.Core.Models;

public class AppUser : IdentityUser<int>, ISoftDelete, IAuditable
{
    [Required, MaxLength(100)]
    public string FullName { get; set; } = string.Empty;

    public int? DepartmentId { get; set; }
    public int? CustomerId { get; set; }

    public bool IsActive { get; set; } = true;

    public bool IsDeleted { get; set; } = false;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }
    public DateTime? DeletedAt { get; set; }

    public DateTime? LastLoginAt { get; set; }

    // Navigation
    [ForeignKey(nameof(DepartmentId))]
    public Department? Department { get; set; }

    [ForeignKey(nameof(CustomerId))]
    public Customer? Customer { get; set; }
}
