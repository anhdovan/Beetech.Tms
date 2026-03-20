using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public abstract class BaseEntity : ISoftDelete, IAuditable
{
    public int Id { get; set; }
    public bool IsDeleted { get; set; } = false;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }
    public DateTime? DeletedAt { get; set; }
}
