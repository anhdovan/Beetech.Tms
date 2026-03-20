using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace Beetech.Tms.Core.Models;

public class AppRole : IdentityRole<int>
{
    [MaxLength(200)]
    public string? Description { get; set; }

    public bool IsActive { get; set; } = true;
}
