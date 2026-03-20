using Beetech.Tms.Core.Data;
using Beetech.Tms.Core.Models;
using Microsoft.EntityFrameworkCore;
using Beetech.Tms.Core.Utils;

namespace Beetech.Tms.Web.Services;

public class TmsService
{
    private readonly TmsDbContext _context;

    public TmsService(TmsDbContext context)
    {
        _context = context;
    }

    // Categories
    public async Task<List<Category>> GetCategoriesAsync() => 
        await _context.Categories.Include(c => c.Items).OrderBy(c => c.Name).ToListAsync();

    public async Task<bool> UpsertCategoryAsync(Category category)
    {
        if (category.Id == 0) _context.Categories.Add(category);
        else _context.Categories.Update(category);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> DeleteCategoryAsync(int id)
    {
        var category = await _context.Categories.FindAsync(id);
        if (category == null) return false;
        _context.Categories.Remove(category);
        return await _context.SaveChangesAsync() > 0;
    }

    // Locations
    public async Task<List<Location>> GetLocationsAsync() => 
        await _context.Locations.Include(l => l.Items).OrderBy(l => l.Name).ToListAsync();

    public async Task<bool> UpsertLocationAsync(Location location)
    {
        if (location.Id == 0) _context.Locations.Add(location);
        else _context.Locations.Update(location);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> DeleteLocationAsync(int id)
    {
        var location = await _context.Locations.FindAsync(id);
        if (location == null) return false;
        _context.Locations.Remove(location);
        return await _context.SaveChangesAsync() > 0;
    }

    // Departments
    public async Task<List<Department>> GetDepartmentsAsync() => 
        await _context.Departments.Include(d => d.Users).OrderBy(d => d.Name).ToListAsync();

    public async Task<bool> UpsertDepartmentAsync(Department department)
    {
        if (department.Id == 0) _context.Departments.Add(department);
        else _context.Departments.Update(department);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> DeleteDepartmentAsync(int id)
    {
        var department = await _context.Departments.FindAsync(id);
        if (department == null) return false;
        _context.Departments.Remove(department);
        return await _context.SaveChangesAsync() > 0;
    }

    // TextileItems
    public async Task<List<TextileItem>> GetItemsAsync(string? searchText = null)
    {
        var query = _context.TextileItems
            .Include(i => i.Category)
            .Include(i => i.CurrentLocation)
            .Include(i => i.CurrentDepartment)
            .AsQueryable();

        if (!string.IsNullOrEmpty(searchText))
        {
            query = query.Where(i => i.Category!.Name.Contains(searchText));
        }

        return await query.OrderByDescending(i => i.LastScanAt).ToListAsync();
    }

    public async Task<TextileItem?> GetItemByIdAsync(int id) => 
        await _context.TextileItems
            .Include(i => i.Category)
            .Include(i => i.CurrentLocation)
            .Include(i => i.CurrentDepartment)
            .FirstOrDefaultAsync(i => i.Id == id);

    public async Task<bool> UpsertItemAsync(TextileItem item)
    {
        if (item.Id == 0) _context.TextileItems.Add(item);
        else _context.TextileItems.Update(item);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<TextileItem?> GetItemByRfidAsync(string rfidOrCode)
    {
        // Try decrypting as potential Code (used for both barcode and RFID)
        (long id, string? tagType) = ChaCha20Util.DecryptExt(rfidOrCode);
        if (id > 0 && tagType == RfidTagConstants.AssetTag)
        {
            return await GetItemByIdAsync((int)id);
        }

        return null;
    }

    public async Task<bool> DeleteItemAsync(int id)
    {
        var item = await _context.TextileItems.FindAsync(id);
        if (item == null) return false;
        _context.TextileItems.Remove(item);
        return await _context.SaveChangesAsync() > 0;
    }

    // Transactions
    public async Task<List<Transaction>> GetTransactionsAsync() =>
        await _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.CreatedBy)
            .Include(t => t.Items)
                .ThenInclude(ti => ti.TextileItem)
                    .ThenInclude(i => i.Category)
            .OrderByDescending(t => t.TransactionDate)
            .ToListAsync();

    public async Task<Transaction?> GetTransactionByIdAsync(int id) =>
        await _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.CreatedBy)
            .Include(t => t.Items)
                .ThenInclude(ti => ti.TextileItem)
                    .ThenInclude(i => i.Category)
            .FirstOrDefaultAsync(t => t.Id == id);

    public async Task<bool> UpsertTransactionAsync(Transaction transaction)
    {
        using var dbTransaction = await _context.Database.BeginTransactionAsync();
        try
        {
            if (string.IsNullOrEmpty(transaction.TransactionNumber))
            {
                transaction.TransactionNumber = $"TR-{DateTime.Now:yyyyMMddHHmmss}";
            }

            if (transaction.Id == 0)
            {
                _context.Transactions.Add(transaction);
            }
            else
            {
                _context.Transactions.Update(transaction);
            }

            await _context.SaveChangesAsync();

            // Handle Item Location/Status updates based on transaction
            foreach (var transItem in transaction.Items)
            {
                var item = await _context.TextileItems.FindAsync(transItem.TextileItemId);
                if (item != null)
                {
                    if (transaction.ToLocationId.HasValue)
                    {
                        item.CurrentLocationId = transaction.ToLocationId.Value;
                    }

                    // Update status based on transaction type
                    item.Status = transaction.Type switch
                    {
                        TransactionType.Delivery => ItemStatus.InUse,
                        TransactionType.Receipt => ItemStatus.Available,
                        TransactionType.LaundrySend => ItemStatus.Soiled,
                        TransactionType.LaundryReceive => ItemStatus.Available,
                        TransactionType.Discard => ItemStatus.Condemned,
                        _ => item.Status
                    };
                    
                    item.LastScanAt = DateTime.UtcNow;
                }
            }

            await _context.SaveChangesAsync();
            await dbTransaction.CommitAsync();
            return true;
        }
        catch (Exception)
        {
            await dbTransaction.RollbackAsync();
            return false;
        }
    }

    public async Task<bool> DeleteTransactionAsync(int id)
    {
        var transaction = await _context.Transactions.FindAsync(id);
        if (transaction == null) return false;
        _context.Transactions.Remove(transaction);
        return await _context.SaveChangesAsync() > 0;
    }

    // Dashboard Stats
    public async Task<DashboardStats> GetDashboardStatsAsync()
    {
        return new DashboardStats
        {
            TotalItems = await _context.TextileItems.CountAsync(),
            AvailableItems = await _context.TextileItems.CountAsync(i => i.Status == ItemStatus.Available),
            InUseItems = await _context.TextileItems.CountAsync(i => i.Status == ItemStatus.InUse),
            SoiledItems = await _context.TextileItems.CountAsync(i => i.Status == ItemStatus.Soiled),
            CondemnedItems = await _context.TextileItems.CountAsync(i => i.Status == ItemStatus.Condemned),
            RecentTransactions = await _context.Transactions.CountAsync(t => t.TransactionDate >= DateTime.UtcNow.AddDays(-7))
        };
    }
}

public class DashboardStats
{
    public int TotalItems { get; set; }
    public int AvailableItems { get; set; }
    public int InUseItems { get; set; }
    public int SoiledItems { get; set; }
    public int CondemnedItems { get; set; }
    public int RecentTransactions { get; set; }
}
