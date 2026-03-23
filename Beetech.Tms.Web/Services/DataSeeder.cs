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
        var roles = new[] { "Admin", "LaundryStaff", "HotelHousekeeping", "HospitalInventoryManager", "Customer" };
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
            new { Email = "laundry@beetech.com", Name = "Laundry Operator", Role = "LaundryStaff" },
            new { Email = "housekeeping@beetech.com", Name = "Hotel Housekeeping", Role = "HotelHousekeeping" },
            new { Email = "manager@hospital.com", Name = "Hospital Inventory Manager", Role = "HospitalInventoryManager" }
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
            new Location { Name = "Sảnh Chờ Tầng 1", Type = LocationType.Hotel, IsActive = true },
            new Location { Name = "Khu vực Giặt", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Khu vực Sấy", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Khu vực Ủi", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Khu vực Gấp", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Khu vực Đóng gói", Type = LocationType.Laundry, IsActive = true },
            new Location { Name = "Khu vực Giao trả", Type = LocationType.Laundry, IsActive = true }
        };
        await _context.Locations.AddRangeAsync(locations);
        await _context.SaveChangesAsync();

        // Seed Customers
        var customers = new[]
        {
            new Customer { Name = "Khách sạn Marriott", Code = "MARRIOTT", Address = "84 Duy Tân", ContactPerson = "Ms. Lan", ContactPhone = "0901234567", IsInternal = false, IsActive = true },
            new Customer { Name = "Bệnh viện Đa khoa", Code = "HOSPITAL", Address = "12 Chu Văn An", ContactPerson = "Mr. Tuấn", ContactPhone = "0987654321", IsInternal = false, IsActive = true },
            new Customer { Name = "Nội bộ - Khối Hành chính", Code = "INTERNAL_ADMIN", Address = "Tầng 5", IsInternal = true, IsActive = true }
        };
        await _context.Customers.AddRangeAsync(customers);
        await _context.SaveChangesAsync();

        // Seed Customer Users
        var customerUsers = new[]
        {
            new { Email = "marriott@beetech.com", Name = "Marriott Admin", Role = "Customer", CustomerId = customers[0].Id },
            new { Email = "hospital@beetech.com", Name = "Hospital Admin", Role = "Customer", CustomerId = customers[1].Id }
        };

        foreach (var u in customerUsers)
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
                    EmailConfirmed = true,
                    CustomerId = u.CustomerId
                };
                var result = await _userManager.CreateAsync(user, "User@123");
                if (result.Succeeded)
                {
                    await _userManager.AddToRoleAsync(user, u.Role);
                }
            }
        }


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
                var dept = departments[random.Next(departments.Length)];
                await _context.TextileItems.AddAsync(new TextileItem
                {
                    CategoryId = category.Id,
                    Status = ItemStatus.Available,
                    CurrentLocationId = mainLocation,
                    CurrentDepartmentId = dept.Id,
                    WashCount = random.Next(0, 10),
                    LastScanAt = DateTime.UtcNow
                });
            }
        }

        await _context.SaveChangesAsync();

        // Seed some Transactions
        var allItems = await _context.TextileItems.ToListAsync();
        var laundryLocation = (await _context.Locations.FirstOrDefaultAsync(l => l.Type == LocationType.Laundry))?.Id;
        
        var transactions = new List<Transaction>();
        
        // 1. Send items to Laundry
        var tx1 = new Transaction
        {
            TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-001",
            Type = TransactionType.LaundrySend,
            FromLocationId = mainLocation,
            ToLocationId = laundryLocation,
            TransactionDate = DateTime.UtcNow.AddDays(-1),
            Notes = "Seeded: Sending to laundry"
        };
        
        // Pick 5 random items for this transaction
        var txItems1 = allItems.OrderBy(x => Guid.NewGuid()).Take(5).Select(item => new TransactionItem
        {
            TextileItemId = item.Id,
            StatusAtTransaction = ItemStatus.Soiled
        }).ToList();
        
        foreach (var ti in txItems1) tx1.Items.Add(ti);
        transactions.Add(tx1);

        // Update item status for these 5 items
        foreach (var ti in txItems1)
        {
            var textile = allItems.First(i => i.Id == ti.TextileItemId);
            textile.Status = ItemStatus.Soiled;
            textile.CurrentLocationId = laundryLocation; 
        }

        // 2. Receive from Laundry
        var tx2 = new Transaction
        {
            TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-002",
            Type = TransactionType.LaundryReceive,
            FromLocationId = laundryLocation,
            ToLocationId = mainLocation,
            TransactionDate = DateTime.UtcNow.AddHours(-2),
            Notes = "Seeded: Received from laundry"
        };

        // Pick 3 other random items for this
        var txItems2 = allItems.OrderBy(x => Guid.NewGuid()).Take(3).Select(item => new TransactionItem
        {
            TextileItemId = item.Id,
            StatusAtTransaction = ItemStatus.Available
        }).ToList();
        
        foreach (var ti in txItems2) tx2.Items.Add(ti);
        transactions.Add(tx2);

        // Update item status
        foreach (var ti in txItems2)
        {
            var textile = allItems.First(i => i.Id == ti.TextileItemId);
            textile.Status = ItemStatus.Available;
            textile.CurrentLocationId = mainLocation;
            textile.WashCount++;
        }

        await _context.Transactions.AddRangeAsync(transactions);

        // 3. Delivery to Department
        var dept1 = departments[0];
        var tx3 = new Transaction
        {
            TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-003",
            Type = TransactionType.Delivery,
            FromLocationId = mainLocation,
            DepartmentId = dept1.Id,
            TransactionDate = DateTime.UtcNow.AddMinutes(-30),
            Notes = $"Seeded: Delivery to {dept1.Name}"
        };

        var txItems3 = allItems.Where(i => i.Status == ItemStatus.Available).OrderBy(x => Guid.NewGuid()).Take(4).Select(item => new TransactionItem
        {
            TextileItemId = item.Id,
            StatusAtTransaction = ItemStatus.InUse
        }).ToList();

        foreach (var ti in txItems3) tx3.Items.Add(ti);
        foreach (var ti in txItems3)
        {
            var textile = allItems.First(i => i.Id == ti.TextileItemId);
            textile.Status = ItemStatus.InUse;
            textile.CurrentDepartmentId = dept1.Id;
        }
        await _context.Transactions.AddAsync(tx3);

        // 4. Internal Transfer between departments
        var dept2 = departments[1];
        var tx4 = new Transaction
        {
            TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-004",
            Type = TransactionType.InternalTransfer,
            DepartmentId = dept2.Id, // Moved to this dept
            TransactionDate = DateTime.UtcNow.AddMinutes(-10),
            Notes = $"Seeded: Internal Transfer to {dept2.Name}"
        };

        var txItems4 = allItems.Where(i => i.Status == ItemStatus.InUse).OrderBy(x => Guid.NewGuid()).Take(2).Select(item => new TransactionItem
        {
            TextileItemId = item.Id,
            StatusAtTransaction = ItemStatus.InUse
        }).ToList();

        foreach (var ti in txItems4) tx4.Items.Add(ti);
        foreach (var ti in txItems4)
        {
            var textile = allItems.First(i => i.Id == ti.TextileItemId);
            textile.CurrentDepartmentId = dept2.Id;
        }
        await _context.Transactions.AddAsync(tx4);

        // 5. Another history entry for one of the items to show tracing
        var tx5 = new Transaction
        {
            TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-005",
            Type = TransactionType.LaundryReceive,
            ToLocationId = locations[0].Id, // Back to central
            TransactionDate = DateTime.UtcNow.AddMinutes(-5),
            Notes = "Seeded: Second history entry for tracing demo"
        };
        var itemToTrace = allItems[0];
        tx5.Items.Add(new TransactionItem { TextileItemId = itemToTrace.Id, StatusAtTransaction = ItemStatus.Available });
        await _context.Transactions.AddAsync(tx5);

        await _context.SaveChangesAsync();

        // 6-10. Complete Laundry Cycle Sequence
        var laundryReceiveLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Xưởng Giặt"))?.Id;
        var washingLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Giặt"))?.Id;
        var dryingLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Sấy"))?.Id;
        var ironingLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Ủi"))?.Id;
        var foldingLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Gấp"))?.Id;
        var packingLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Đóng gói"))?.Id;
        var returnLocId = (await _context.Locations.FirstOrDefaultAsync(l => l.Name == "Khu vực Giao trả"))?.Id;

        var marriott = await _context.Customers.FirstOrDefaultAsync(c => c.Code == "MARRIOTT");

        if (laundryReceiveLocId != null && washingLocId != null)
        {
            // 6. Laundry Receive from Hotel
            var tx6 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-006",
                Type = TransactionType.LaundryReceive,
                ToLocationId = laundryReceiveLocId,
                CustomerId = marriott?.Id,
                TransactionDate = DateTime.UtcNow.AddHours(-10),
                Description = "Morning batch from Marriot Hotel",
                Notes = "Seeded: Workflow Start - Laundry Receive"
            };
            var itemsForCycle = allItems.Skip(10).Take(10).ToList();
            for (int i = 0; i < itemsForCycle.Count; i++)
            {
                var item = itemsForCycle[i];
                var ti = new TransactionItem 
                { 
                    TextileItemId = item.Id, 
                    StatusAtTransaction = ItemStatus.Soiled,
                    Notes = (i == 0) ? "Wine stain - special treatment needed" : null
                };
                tx6.Items.Add(ti);
                item.Status = ItemStatus.Soiled;
                item.CurrentLocationId = laundryReceiveLocId;
                item.CurrentCustomerId = marriott?.Id;
            }
            await _context.Transactions.AddAsync(tx6);
            await _context.SaveChangesAsync(); // Save to get Id

            // 7. Washing (Source: TX6)
            var tx7 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-007",
                Type = TransactionType.Washing,
                ToLocationId = washingLocId,
                SourceTransactionId = tx6.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddHours(-8),
                Description = "Batch Marriot - Heavy wash cycle",
                Notes = "Seeded: Workflow - Washing"
            };
            foreach (var item in itemsForCycle)
            {
                tx7.Items.Add(new TransactionItem { TextileItemId = item.Id, StatusAtTransaction = ItemStatus.Washing });
                item.Status = ItemStatus.Washing;
                item.CurrentLocationId = washingLocId;
            }
            await _context.Transactions.AddAsync(tx7);
            await _context.SaveChangesAsync();

            // 8. Drying (Source: TX7)
            var tx8 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-008",
                Type = TransactionType.Drying,
                ToLocationId = dryingLocId,
                SourceTransactionId = tx7.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddHours(-6),
                Description = "Batch Marriot - High temp drying",
                Notes = "Seeded: Workflow - Drying"
            };
            foreach (var item in itemsForCycle)
            {
                tx8.Items.Add(new TransactionItem { TextileItemId = item.Id, StatusAtTransaction = ItemStatus.Drying });
                item.Status = ItemStatus.Drying;
                item.CurrentLocationId = dryingLocId;
            }
            await _context.Transactions.AddAsync(tx8);
            await _context.SaveChangesAsync();

            // 9. Ironing (Source: TX8)
            var tx9 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-009",
                Type = TransactionType.Ironing,
                ToLocationId = ironingLocId,
                SourceTransactionId = tx8.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddHours(-4),
                Notes = "Seeded: Workflow - Ironing"
            };
            foreach (var item in itemsForCycle)
            {
                tx9.Items.Add(new TransactionItem { TextileItemId = item.Id, StatusAtTransaction = ItemStatus.Ironing });
                item.Status = ItemStatus.Ironing;
                item.CurrentLocationId = ironingLocId;
            }
            await _context.Transactions.AddAsync(tx9);
            await _context.SaveChangesAsync();

            // 10. Folding (Source: TX9)
            var tx10 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-010",
                Type = TransactionType.Folding,
                ToLocationId = foldingLocId,
                SourceTransactionId = tx9.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddHours(-2),
                Notes = "Seeded: Workflow - Folding"
            };
            foreach (var item in itemsForCycle)
            {
                tx10.Items.Add(new TransactionItem { TextileItemId = item.Id, StatusAtTransaction = ItemStatus.Folding });
                item.Status = ItemStatus.Folding;
                item.CurrentLocationId = foldingLocId;
            }
            await _context.Transactions.AddAsync(tx10);
            await _context.SaveChangesAsync();

            // 11. Packing (Source: TX10)
            var tx11 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-011",
                Type = TransactionType.Packing,
                ToLocationId = packingLocId,
                SourceTransactionId = tx10.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddHours(-1),
                Notes = "Seeded: Workflow - Packing into containers"
            };

            var box1 = new PackingUnit { Code = "BOX-MAR-001", Type = PackageType.Carton, Weight = 12.5m };
            var bag1 = new PackingUnit { Code = "BAG-MAR-001", Type = PackageType.NilonBag, Weight = 4.2m };
            tx11.PackingUnits.Add(box1);
            tx11.PackingUnits.Add(bag1);

            for (int i = 0; i < itemsForCycle.Count; i++)
            {
                var item = itemsForCycle[i];
                var ti = new TransactionItem 
                { 
                    TextileItemId = item.Id, 
                    StatusAtTransaction = ItemStatus.Packing,
                    PackingUnit = (i < 7) ? box1 : bag1 // Split items between box and bag
                };
                tx11.Items.Add(ti);
                item.Status = ItemStatus.Packing;
                item.CurrentLocationId = packingLocId;
            }
            await _context.Transactions.AddAsync(tx11);
            await _context.SaveChangesAsync();

            // 12. Return/Delivery (Source: TX11)
            var tx12 = new Transaction
            {
                TransactionNumber = $"TX-{DateTime.UtcNow:yyyyMMdd}-012",
                Type = TransactionType.Return,
                ToLocationId = returnLocId,
                CustomerId = marriott?.Id,
                SourceTransactionId = tx11.Id,
                TargetQuantity = itemsForCycle.Count,
                TransactionDate = DateTime.UtcNow.AddMinutes(-15),
                Description = "Delivery back to Marriot",
                Notes = "Seeded: Workflow End - Return Delivery"
            };
            foreach (var item in itemsForCycle)
            {
                tx12.Items.Add(new TransactionItem { TextileItemId = item.Id, StatusAtTransaction = ItemStatus.Returned });
                item.Status = ItemStatus.Returned;
                item.CurrentLocationId = returnLocId;
            }
            await _context.Transactions.AddAsync(tx12);
            await _context.SaveChangesAsync();
        }
    }
}
