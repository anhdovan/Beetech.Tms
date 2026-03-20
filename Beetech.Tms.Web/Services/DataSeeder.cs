using Beetech.Tms.Core.Data;
using Beetech.Tms.Core.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace Beetech.Tms.Web.Services;

public class DataSeeder
{
    private readonly TmsDbContext _context;
    private readonly UserManager<AppUser> _userManager;
    private readonly RoleManager<AppRole> _roleManager;

    public DataSeeder(TmsDbContext context, UserManager<AppUser> userManager, RoleManager<AppRole> roleManager)
    {
        _context = context;
        _userManager = userManager;
        _roleManager = roleManager;
    }

    public async Task SeedAsync()
    {
        await _context.Database.EnsureCreatedAsync();

        if (await _context.Categories.AnyAsync()) return;

        // Seed Roles
        var roles = new[] { "Admin", "Manager", "Operator", "Customer", "Laundry" };
        foreach (var role in roles)
        {
            if (!await _roleManager.RoleExistsAsync(role))
            {
                await _roleManager.CreateAsync(new AppRole { Name = role, Description = $"{role} Role" });
            }
        }

        // Seed Sample Users for each role
        var sampleUsers = new[]
        {
            new { Email = "admin@beetech.com", Name = "System Admin", Role = "Admin" },
            new { Email = "manager@hospital.com", Name = "Hospital Manager", Role = "Manager" },
            new { Email = "operator@hospital.com", Name = "Hospital Operator", Role = "Operator" },
            new { Email = "customer@hospital.com", Name = "Department Lead", Role = "Customer" },
            new { Email = "laundry@clean.com", Name = "Laundry Manager", Role = "Laundry" }
        };

        foreach (var u in sampleUsers)
        {
            var user = await _userManager.FindByEmailAsync(u.Email);
            if (user == null)
            {
                user = new AppUser
                {
                    UserName = u.Email,
                    Email = u.Email,
                    FullName = u.Name,
                    IsActive = true,
                    EmailConfirmed = true
                };
                var result = await _userManager.CreateAsync(user, "User@123");
                if (result.Succeeded)
                {
                    await _userManager.AddToRoleAsync(user, u.Role);
                }
            }
        }

        // Seed Departments
        var departments = new[]
        {
            new Department { Name = "Khoa Nhi", IsActive = true },
            new Department { Name = "Khoa Ngoại", IsActive = true },
            new Department { Name = "Khoa Nội", IsActive = true },
            new Department { Name = "Phòng Mổ", IsActive = true },
            new Department { Name = "Khoa Sản", IsActive = true }
        };
        await _context.Departments.AddRangeAsync(departments);
        await _context.SaveChangesAsync();

        // Seed Locations
        var locations = new[]
        {
            new Location { Name = "Kho Sạch Trung Tâm", Type = LocationType.Storage, IsActive = true },
            new Location { Name = "Kho Đồ Bẩn", Type = LocationType.Storage, IsActive = true },
            new Location { Name = "Xưởng Giặt", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Sảnh Chờ Tầng 1", Type = LocationType.Hotel, IsActive = true }
        };
        await _context.Locations.AddRangeAsync(locations);
        await _context.SaveChangesAsync();

        // Seed Categories from Image
        var categories = new[]
        {
            new Category { Name = "Áo choàng BN size 1 (sơ sinh)", Description = "Áo ngắn tay, cổ tròn, thắt dây", IsActive = true },
            new Category { Name = "Áo choàng BN size 2 (<10kg)", Description = "Áo ngắn tay, cổ tròn, thắt dây", IsActive = true },
            new Category { Name = "Áo choàng BN size 3 (10-15kg)", Description = "Áo ngắn tay, cổ tròn, thắt dây", IsActive = true },
            new Category { Name = "Áo choàng BN size 4 (15-20kg)", Description = "Áo ngắn tay, cổ tròn, thắt dây", IsActive = true },
            new Category { Name = "Áo choàng BN size 5 (20-30kg)", Description = "Áo ngắn tay, cổ tròn, thắt dây", IsActive = true },
            new Category { Name = "Áo quần BN size 6 (30-50kg)", Description = "QABN bộ", IsActive = true },
            new Category { Name = "Áo choàng thủ thuật", Description = "Áo choàng", IsActive = true },
            new Category { Name = "Áo choàng phòng mổ", Description = "Áo choàng", IsActive = true },
            new Category { Name = "Drap giường bệnh nhân 1 lớp", Description = "1,5m x 2,5m", IsActive = true },
            new Category { Name = "Drap trải giường nôi 1 lớp", Description = "1m x 1m", IsActive = true },
            new Category { Name = "Áo gối", Description = "40cm x 60cm", IsActive = true },
            new Category { Name = "Mền chần gòn", Description = "1.2m x 1.6m", IsActive = true },
            new Category { Name = "Khăn tắm lớn", Description = "60cm x 160cm", IsActive = true },
            new Category { Name = "Khăn lau tay 2 lớp", Description = "0,4m x 0,4m", IsActive = true },
            new Category { Name = "Nón cap size vừa 2 lớp (Size M)", Description = "50cm x 12cm", IsActive = true }
        };
        await _context.Categories.AddRangeAsync(categories);
        await _context.SaveChangesAsync();

        // Seed Sample Items for each Category
        var random = new Random();
        var allCategories = await _context.Categories.ToListAsync();
        var mainLocation = locations[0].Id;

        foreach (var category in allCategories)
        {
            // Seed 3 items for each category
            for (int i = 1; i <= 3; i++)
            {
                var tag = $"E200{category.Id:D3}{i:D2}";
                await _context.TextileItems.AddAsync(new TextileItem
                {
                    CategoryId = category.Id,
                    Status = ItemStatus.Available,
                    CurrentLocationId = mainLocation,
                    WashCount = random.Next(0, 10),
                    LastScanAt = DateTime.UtcNow
                });
            }
        }

        await _context.SaveChangesAsync();
    }
}
