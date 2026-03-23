using Beetech.Tms.Core.Data;
using Beetech.Tms.Core.Models;
using Microsoft.EntityFrameworkCore;
using Beetech.Tms.Core.Utils;
using OfficeOpenXml;
using OfficeOpenXml.Table;

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

    // Customers
    public async Task<List<Customer>> GetCustomersAsync() => 
        await _context.Customers.Include(c => c.Items).OrderBy(c => c.Name).ToListAsync();

    public async Task<bool> UpsertCustomerAsync(Customer customer)
    {
        if (customer.Id == 0) _context.Customers.Add(customer);
        else _context.Customers.Update(customer);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> DeleteCustomerAsync(int id)
    {
        var customer = await _context.Customers.FindAsync(id);
        if (customer == null) return false;
        _context.Customers.Remove(customer);
        return await _context.SaveChangesAsync() > 0;
    }

    // TextileItems
    public async Task<List<TextileItem>> GetItemsAsync(string? searchText = null)
    {
        var query = _context.TextileItems
            .Include(i => i.Category)
            .Include(i => i.CurrentLocation)
            .Include(i => i.CurrentDepartment)
            .Include(i => i.CurrentCustomer)
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
            .Include(i => i.CurrentCustomer)
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

    public async Task<List<TextileItem>> BulkRegisterItemsAsync(int categoryId, int? locationId, int? departmentId, int count)
    {
        var items = new List<TextileItem>();
        for (int i = 0; i < count; i++)
        {
            var item = new TextileItem
            {
                CategoryId = categoryId,
                CurrentLocationId = locationId,
                CurrentDepartmentId = departmentId,
                Status = ItemStatus.Unregistered,
                LastScanAt = DateTime.UtcNow
            };
            _context.TextileItems.Add(item);
            items.Add(item);
        }
        await _context.SaveChangesAsync();
        return items;
    }

    public async Task<bool> ConfirmRegistrationAsync(List<int> itemIds)
    {
        var items = await _context.TextileItems
            .Where(i => itemIds.Contains(i.Id) && i.Status == ItemStatus.Unregistered)
            .ToListAsync();

        foreach (var item in items)
        {
            item.Status = ItemStatus.Available;
        }

        return await _context.SaveChangesAsync() > 0;
    }

    // Transactions
    public async Task<List<Transaction>> GetTransactionsAsync(int? customerId = null)
    {
        var query = _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.Customer)
            .Include(t => t.CreatedBy)
            .Include(t => t.Items)
                .ThenInclude(ti => ti.TextileItem)
                    .ThenInclude(i => i.Category)
            .Include(t => t.PackingUnits)
            .AsQueryable();

        if (customerId.HasValue)
        {
            query = query.Where(t => t.CustomerId == customerId.Value);
        }

        return await query.OrderByDescending(t => t.TransactionDate).ToListAsync();
    }

    public async Task<Transaction?> GetTransactionByIdAsync(int id) =>
        await _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.Customer)
            .Include(t => t.CreatedBy)
            .Include(t => t.Items)
                .ThenInclude(ti => ti.TextileItem)
                    .ThenInclude(i => i.Category)
            .Include(t => t.PackingUnits)
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

            // Populate TargetQuantity if not set and it's a processing stage
            if (transaction.TargetQuantity == 0)
            {
                ItemStatus? sourceStatus = null;
                if (transaction.Type == TransactionType.Washing) sourceStatus = ItemStatus.Soiled;
                else if (transaction.Type == TransactionType.Drying) sourceStatus = ItemStatus.Washing;
                else if (transaction.Type == TransactionType.Ironing) sourceStatus = ItemStatus.Drying;
                else if (transaction.Type == TransactionType.Folding) sourceStatus = ItemStatus.Ironing;
                else if (transaction.Type == TransactionType.Packing) sourceStatus = ItemStatus.Folding;
                else if (transaction.Type == TransactionType.Return) sourceStatus = ItemStatus.Packing;

                if (sourceStatus.HasValue)
                {
                    transaction.TargetQuantity = await _context.TextileItems.CountAsync(i => i.Status == sourceStatus.Value);
                    await _context.SaveChangesAsync(); // Update the transaction with the target quantity
                }
            }

            // Update item states based on transaction type
            foreach (var ti in transaction.Items)
            {
                var item = await _context.TextileItems.FindAsync(ti.TextileItemId);
                if (item != null)
                {
                    if (transaction.Type == TransactionType.LaundrySend)
                    {
                        item.Status = ItemStatus.Soiled;
                        item.CurrentLocationId = transaction.ToLocationId;
                    }
                    else if (transaction.Type == TransactionType.LaundryReceive)
                    {
                        item.Status = ItemStatus.Soiled;
                        item.CurrentLocationId = transaction.ToLocationId;
                        if (transaction.CustomerId.HasValue) item.CurrentCustomerId = transaction.CustomerId;
                        item.WashCount++;
                    }
                    else if (transaction.Type == TransactionType.Delivery)
                    {
                        item.Status = ItemStatus.InUse;
                        item.CurrentDepartmentId = transaction.DepartmentId;
                    }
                    else if (transaction.Type == TransactionType.Receipt)
                    {
                        item.Status = ItemStatus.Available;
                        item.CurrentDepartmentId = null;
                    }
                    else if (transaction.Type == TransactionType.InternalTransfer)
                    {
                        if (transaction.DepartmentId.HasValue) item.CurrentDepartmentId = transaction.DepartmentId;
                        if (transaction.ToLocationId.HasValue) item.CurrentLocationId = transaction.ToLocationId;
                    }
                    else if (transaction.Type == TransactionType.Discard)
                    {
                        item.Status = ItemStatus.Condemned;
                    }
                    else if (transaction.Type == TransactionType.Washing)
                    {
                        item.Status = ItemStatus.Washing;
                    }
                    else if (transaction.Type == TransactionType.Drying)
                    {
                        item.Status = ItemStatus.Drying;
                    }
                    else if (transaction.Type == TransactionType.Ironing)
                    {
                        item.Status = ItemStatus.Ironing;
                    }
                    else if (transaction.Type == TransactionType.Folding)
                    {
                        item.Status = ItemStatus.Folding;
                    }
                    else if (transaction.Type == TransactionType.Packing)
                    {
                        item.Status = ItemStatus.Packing;
                    }
                    else if (transaction.Type == TransactionType.Return)
                    {
                        item.Status = ItemStatus.Returned;
                        item.CurrentLocationId = transaction.ToLocationId;
                        item.CurrentDepartmentId = transaction.DepartmentId;
                    }

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

    public async Task<List<Transaction>> GetItemTransactionsAsync(int itemId)
    {
        return await _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.CreatedBy)
            .Where(t => t.Items.Any(ti => ti.TextileItemId == itemId))
            .OrderByDescending(t => t.TransactionDate)
            .ToListAsync();
    }

    public async Task<List<Transaction>> GetCustomerTransactionsAsync(int customerId)
    {
        return await _context.Transactions
            .Include(t => t.FromLocation)
            .Include(t => t.ToLocation)
            .Include(t => t.Department)
            .Include(t => t.CreatedBy)
            .Where(t => t.CustomerId == customerId)
            .OrderByDescending(t => t.TransactionDate)
            .ToListAsync();
    }

    // Excel Export/Import
    public async Task<byte[]> ExportItemsToExcelAsync()
    {
        ExcelPackage.License.SetNonCommercialPersonal("Beetech TMS");
        using var package = new ExcelPackage();
        var worksheet = package.Workbook.Worksheets.Add("Items");

        var items = await _context.TextileItems
            .Include(i => i.Category)
            .Include(i => i.CurrentLocation)
            .Include(i => i.CurrentDepartment)
            .ToListAsync();

        var exportData = items.Select(i => new
        {
            i.Id,
            Code = i.Code,
            Category = i.Category?.Name ?? "",
            Status = i.Status.ToString(),
            Location = i.CurrentLocation?.Name ?? "",
            Department = i.CurrentDepartment?.Name ?? "",
            WashCount = i.WashCount,
            LastScan = i.LastScanAt.ToString("yyyy-MM-dd HH:mm:ss")
        }).ToList();

        if (exportData.Any())
        {
            var range = worksheet.Cells["A1"].LoadFromCollection(exportData, true);
            var table = worksheet.Tables.Add(range, "ItemsTable");
            table.TableStyle = TableStyles.Medium2;
            worksheet.Cells.AutoFitColumns();
        }
        else
        {
            worksheet.Cells["A1"].Value = "No items found";
        }

        return await package.GetAsByteArrayAsync();
    }

    public async Task<(int success, int failed, string message)> ImportItemsFromExcelAsync(Stream stream)
    {
        ExcelPackage.License.SetNonCommercialPersonal("Beetech TMS");
        using var package = new ExcelPackage(stream);
        var worksheet = package.Workbook.Worksheets[0];
        if (worksheet == null) return (0, 0, "No worksheet found in Excel file");

        int rowCount = worksheet.Dimension?.Rows ?? 0;
        if (rowCount < 2) return (0, 0, "Excel file is empty or missing headers");

        var categories = await _context.Categories.ToDictionaryAsync(c => c.Name.ToLower(), c => c.Id);
        var locations = await _context.Locations.ToDictionaryAsync(l => l.Name.ToLower(), l => l.Id);
        var departments = await _context.Departments.ToDictionaryAsync(d => d.Name.ToLower(), d => d.Id);

        int success = 0;
        int failed = 0;
        var errors = new List<string>();

        // Columns Map (Header -> Index)
        var headers = new Dictionary<string, int>();
        for (int col = 1; col <= worksheet.Dimension!.Columns; col++)
        {
            var header = worksheet.Cells[1, col].Text.Trim().ToLower();
            if (!string.IsNullOrEmpty(header)) headers[header] = col;
        }

        for (int row = 2; row <= rowCount; row++)
        {
            try
            {
                var idStr = headers.ContainsKey("id") ? worksheet.Cells[row, headers["id"]].Text : "";
                int.TryParse(idStr, out int id);

                var categoryName = headers.ContainsKey("category") ? worksheet.Cells[row, headers["category"]].Text.Trim() : "";
                var locationName = headers.ContainsKey("location") ? worksheet.Cells[row, headers["location"]].Text.Trim() : "";
                var departmentName = headers.ContainsKey("department") ? worksheet.Cells[row, headers["department"]].Text.Trim() : "";
                var statusStr = headers.ContainsKey("status") ? worksheet.Cells[row, headers["status"]].Text.Trim() : "";
                var washCountStr = headers.ContainsKey("washcount") ? worksheet.Cells[row, headers["washcount"]].Text.Trim() : "";

                if (string.IsNullOrEmpty(categoryName))
                {
                    failed++;
                    continue;
                }

                if (!categories.TryGetValue(categoryName.ToLower(), out int categoryId))
                {
                    // Optionally create category? For now, skip or log error
                    failed++;
                    continue;
                }

                TextileItem item;
                if (id > 0)
                {
                    item = await _context.TextileItems.FindAsync(id) ?? new TextileItem();
                }
                else
                {
                    item = new TextileItem();
                }

                item.CategoryId = categoryId;
                if (locations.TryGetValue(locationName.ToLower(), out int locId)) item.CurrentLocationId = locId;
                if (departments.TryGetValue(departmentName.ToLower(), out int depId)) item.CurrentDepartmentId = depId;
                if (Enum.TryParse<ItemStatus>(statusStr, true, out var status)) item.Status = status;
                if (int.TryParse(washCountStr, out int washCount)) item.WashCount = washCount;

                if (item.Id == 0) _context.TextileItems.Add(item);
                else _context.TextileItems.Update(item);

                success++;
            }
            catch (Exception ex)
            {
                failed++;
                errors.Add($"Row {row}: {ex.Message}");
            }
        }

        await _context.SaveChangesAsync();
        return (success, failed, errors.Any() ? string.Join("; ", errors.Take(5)) : "");
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
