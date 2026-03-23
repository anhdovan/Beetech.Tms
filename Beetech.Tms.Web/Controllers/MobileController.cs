using Beetech.Tms.Core.Data;
using Beetech.Tms.Core.Models;
using Beetech.Tms.Core.Utils;
using Beetech.Tms.Web.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace Beetech.Tms.Web.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MobileController : ControllerBase
    {
        private readonly TmsDbContext _context;
        private readonly TmsService _tmsService;
        private readonly UserManager<AppUser> _userManager;
        private readonly SignInManager<AppUser> _signInManager;
        private readonly IConfiguration _configuration;

        public MobileController(
            TmsDbContext context,
            TmsService tmsService,
            UserManager<AppUser> userManager,
            SignInManager<AppUser> signInManager,
            IConfiguration configuration)
        {
            _context = context;
            _tmsService = tmsService;
            _userManager = userManager;
            _signInManager = signInManager;
            _configuration = configuration;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var user = await _userManager.FindByEmailAsync(request.Username) ?? await _userManager.FindByNameAsync(request.Username);
            if (user == null) return Unauthorized(new { message = "Invalid username or password" });

            var result = await _signInManager.CheckPasswordSignInAsync(user, request.Password, false);
            if (!result.Succeeded) return Unauthorized(new { message = "Invalid username or password" });

            var token = GenerateJwtToken(user);
            return Ok(new
            {
                token,
                username = user.UserName,
                fullName = user.FullName,
                role = (await _userManager.GetRolesAsync(user)).FirstOrDefault() ?? "Staff"
            });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("locations")]
        public async Task<IActionResult> GetLocations()
        {
            var locations = await _context.Locations
                .OrderBy(l => l.Name)
                .Select(l => new { id = l.Id, name = l.Name })
                .ToListAsync();
            return Ok(locations);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("categories")]
        public async Task<IActionResult> GetCategories()
        {
            var categories = await _context.Categories
                .OrderBy(c => c.Name)
                .Select(c => new { id = c.Id, name = c.Name })
                .ToListAsync();
            return Ok(categories);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("items")]
        public async Task<IActionResult> GetItems([FromQuery] string? search, [FromQuery] int? locationId, [FromQuery] int? departmentId)
        {
            var query = _context.TextileItems
                .Include(i => i.Category)
                .Include(i => i.CurrentLocation)
                .Include(i => i.CurrentDepartment)
                .AsQueryable();

            if (locationId.HasValue)
            {
                query = query.Where(i => i.CurrentLocationId == locationId.Value);
            }

            if (departmentId.HasValue)
            {
                query = query.Where(i => i.CurrentDepartmentId == departmentId.Value);
            }

            // Fetch data first, then apply filter/projection in memory because 'Code' is computed in C#
            var items = await query.ToListAsync();

            if (!string.IsNullOrEmpty(search))
            {
                items = items.Where(i => i.Code.Contains(search, StringComparison.OrdinalIgnoreCase) || 
                                       (i.Category != null && i.Category.Name.Contains(search, StringComparison.OrdinalIgnoreCase)))
                             .ToList();
            }

            var result = items
                .Select(i => new {
                    id = i.Id,
                    code = i.Code,
                    status = i.Status.ToString(),
                    category = i.Category != null ? i.Category.Name : "N/A",
                    location = i.CurrentLocation != null ? i.CurrentLocation.Name : "N/A",
                    department = i.CurrentDepartment != null ? i.CurrentDepartment.Name : "N/A",
                    washCount = i.WashCount,
                    epc = ChaCha20Util.EncryptExt(i.Id, RfidTagConstants.AssetTag)
                })
                .OrderBy(i => i.code)
                .ToList();

            return Ok(result);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("departments")]
        public async Task<IActionResult> GetDepartments()
        {
            var departments = await _tmsService.GetDepartmentsAsync();
            return Ok(departments.Select(d => new { d.Id, d.Name }));
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("customers")]
        public async Task<IActionResult> GetCustomers()
        {
            var customers = await _context.Customers
                .OrderBy(c => c.Name)
                .Select(c => new { id = c.Id, name = c.Name, isInternal = c.IsInternal })
                .ToListAsync();
            return Ok(customers);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("items/by-tag/{epc}")]
        public async Task<IActionResult> GetItemByTag(string epc)
        {
            var (id, type) = ChaCha20Util.DecryptExt(epc);
            if (id <= 0 || type != RfidTagConstants.AssetTag)
            {
                return NotFound(new { message = "Invalid tag or tag not recognized" });
            }

            var item = await _tmsService.GetItemByIdAsync((int)id);
            if (item == null) return NotFound(new { message = "Item not found" });

            return Ok(new
            {
                item.Id,
                item.Code,
                item.Status,
                Category = item.Category?.Name,
                Location = item.CurrentLocation?.Name,
                Department = item.CurrentDepartment?.Name,
                item.WashCount
            });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("transaction")]
        public async Task<IActionResult> SaveTransaction([FromBody] MobileTransactionRequest request)
        {
            var user = await _userManager.FindByNameAsync(User.Identity?.Name ?? "");
            if (user == null) return Unauthorized();

            var transaction = new Transaction
            {
                Type = request.Type,
                FromLocationId = request.FromLocationId,
                ToLocationId = request.ToLocationId,
                DepartmentId = request.DepartmentId,
                CustomerId = request.CustomerId,
                TransactionDate = DateTime.UtcNow,
                CreatedById = user.Id,
                Notes = request.Notes ?? "Mobile Transaction",
                TargetQuantity = request.TargetQuantity,
                SourceTransactionId = request.SourceTransactionId,
                Items = new List<TransactionItem>()
            };

            if (request.PackingUnits != null && request.PackingUnits.Any())
            {
                foreach (var puReq in request.PackingUnits)
                {
                    transaction.PackingUnits.Add(new PackingUnit
                    {
                        Code = puReq.Code,
                        Type = puReq.Type,
                        Weight = puReq.Weight
                    });
                }
            }

            if (request.DetailedItems != null && request.DetailedItems.Any())
            {
                foreach (var itemReq in request.DetailedItems)
                {
                    var (id, type) = ChaCha20Util.DecryptExt(itemReq.Epc);
                    if (id > 0 && type == RfidTagConstants.AssetTag)
                    {
                        var ti = new TransactionItem 
                        { 
                            TextileItemId = (int)id,
                            Notes = itemReq.Notes
                        };

                        if (!string.IsNullOrEmpty(itemReq.PackingUnitCode))
                        {
                            var pu = transaction.PackingUnits.FirstOrDefault(u => u.Code == itemReq.PackingUnitCode);
                            if (pu != null) ti.PackingUnit = pu;
                        }

                        transaction.Items.Add(ti);
                    }
                }
            }
            else
            {
                foreach (var epc in request.Epcs)
                {
                    var (id, type) = ChaCha20Util.DecryptExt(epc);
                    if (id > 0 && type == RfidTagConstants.AssetTag)
                    {
                        transaction.Items.Add(new TransactionItem { TextileItemId = (int)id });
                    }
                }
            }

            if (!transaction.Items.Any()) return BadRequest(new { message = "No valid tags provided" });

            var success = await _tmsService.UpsertTransactionAsync(transaction);
            return success ? Ok(new { message = "Transaction saved successfully", transactionNumber = transaction.TransactionNumber }) 
                           : StatusCode(500, new { message = "Failed to save transaction" });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("inventory/session")]
        public async Task<IActionResult> StartInventorySession([FromBody] InventorySessionRequest request)
        {
            var session = new InventoryAuditSession
            {
                LocationId = request.LocationId,
                PerformByName = User.Identity?.Name ?? "Handheld",
                Status = "In Progress"
            };

            _context.InventoryAuditSessions.Add(session);
            await _context.SaveChangesAsync();

            return Ok(session);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("inventory/records-batch")]
        public async Task<IActionResult> SaveInventoryRecordsBatch([FromBody] List<MobileInventoryRecord> records)
        {
            if (records == null || !records.Any()) return BadRequest();

            var dbRecords = new List<InventoryAuditResult>();
            foreach (var r in records)
            {
                var (id, _) = ChaCha20Util.DecryptExt(r.Tag);
                dbRecords.Add(new InventoryAuditResult
                {
                    InventoryAuditSessionId = r.SessionId,
                    Tag = r.Tag,
                    TextileItemId = id > 0 ? (int)id : null,
                    AssetName = r.AssetName,
                    Status = r.Status,
                    IsValid = r.IsValid,
                    ScanAt = DateTime.UtcNow
                });
            }

            _context.InventoryAuditResults.AddRange(dbRecords);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Records saved successfully", count = dbRecords.Count });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("transactions")]
        public async Task<IActionResult> GetTransactions([FromQuery] TransactionType? type, [FromQuery] int limit = 20)
        {
            var query = _context.Transactions
                .Include(t => t.Items)
                .AsQueryable();

            if (type.HasValue)
            {
                query = query.Where(t => t.Type == type.Value);
            }

            var result = await query
                .OrderByDescending(t => t.TransactionDate)
                .Take(limit)
                .Select(t => new {
                    id = t.Id,
                    transactionNumber = t.TransactionNumber,
                    description = t.Description,
                    type = t.Type.ToString(),
                    transactionDate = t.TransactionDate,
                    itemCount = t.Items.Count
                })
                .ToListAsync();

            return Ok(result);
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("items/map-tag")]
        public async Task<IActionResult> MapTag([FromBody] MapTagRequest request)
        {
            var (id, type) = ChaCha20Util.DecryptExt(request.ItemCode);
            if (id <= 0 || type != RfidTagConstants.AssetTag)
            {
                return BadRequest(new { message = "Invalid Item Code format" });
            }

            var item = await _context.TextileItems
                .Include(i => i.Category)
                .FirstOrDefaultAsync(i => i.Id == (int)id);

            if (item == null) return NotFound(new { message = "Item not found" });

            string expectedEpc = ChaCha20Util.EncryptExt(item.Id, RfidTagConstants.AssetTag);
            
            if (request.Epc != expectedEpc)
            {
                return BadRequest(new { message = "Tag EPC does not match Item ID pattern", expectedEpc });
            }

            return Ok(new { message = "Tag verified and mapped", item.Code, Category = item.Category?.Name });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("items/{id}/status")]
        public async Task<IActionResult> UpdateItemStatus(int id, [FromQuery] string status)
        {
            if (!Enum.TryParse<ItemStatus>(status, true, out var itemStatus))
            {
                return BadRequest(new { message = "Invalid status value" });
            }

            var item = await _context.TextileItems.FindAsync(id);
            if (item == null) return NotFound();

            item.Status = itemStatus;
            await _context.SaveChangesAsync();

            return Ok(new { message = "Item status updated successfully" });
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("items/bulk-register")]
        public async Task<IActionResult> BulkRegisterItems([FromBody] BulkRegisterRequest request)
        {
            if (request.Count <= 0) return BadRequest(new { message = "Count must be greater than 0" });

            var items = await _tmsService.BulkRegisterItemsAsync(
                request.CategoryId, 
                request.LocationId, 
                request.DepartmentId, 
                request.Count);

            return Ok(items.Select(i => new { i.Id, i.Code }));
        }

        [Authorize(AuthenticationSchemes = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("items/confirm-registration")]
        public async Task<IActionResult> ConfirmRegistration([FromBody] ConfirmRegistrationRequest request)
        {
            if (request.ItemIds == null || !request.ItemIds.Any()) return BadRequest(new { message = "No item IDs provided" });

            var success = await _tmsService.ConfirmRegistrationAsync(request.ItemIds);
            return success ? Ok(new { message = "Items confirmed successfully" }) 
                           : StatusCode(500, new { message = "Failed to confirm items" });
        }

        private string GenerateJwtToken(AppUser user)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["Jwt:Key"] ?? "this_is_a_very_secret_key_for_jwt_32_chars_long"));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, user.UserName ?? ""),
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
            };

            var token = new JwtSecurityToken(
                issuer: _configuration["Jwt:Issuer"] ?? "BeetechTMS",
                audience: _configuration["Jwt:Audience"] ?? "BeetechTMS",
                claims: claims,
                expires: DateTime.Now.AddDays(7),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }

    public class LoginRequest
    {
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }

    public class MobileTransactionRequest
    {
        public TransactionType Type { get; set; }
        public int? FromLocationId { get; set; }
        public int? ToLocationId { get; set; }
        public int? DepartmentId { get; set; }
        public List<string> Epcs { get; set; } = new();
        public List<TransactionItemRequest>? DetailedItems { get; set; }
        public string? Notes { get; set; }
        public int TargetQuantity { get; set; }
        public int? SourceTransactionId { get; set; }
        public int? CustomerId { get; set; }
        public List<PackingUnitRequest>? PackingUnits { get; set; }
    }

    public class PackingUnitRequest
    {
        public string Code { get; set; } = string.Empty;
        public PackageType Type { get; set; }
        public decimal? Weight { get; set; }
    }

    public class TransactionItemRequest
    {
        public string Epc { get; set; } = string.Empty;
        public string? Notes { get; set; }
        public string? PackingUnitCode { get; set; }
    }

    public class InventorySessionRequest
    {
        public int LocationId { get; set; }
    }

    public class MobileInventoryRecord
    {
        public int SessionId { get; set; }
        public string Tag { get; set; } = string.Empty;
        public string? AssetName { get; set; }
        public string Status { get; set; } = string.Empty;
        public bool IsValid { get; set; }
    }

    public class MapTagRequest
    {
        public string Epc { get; set; } = string.Empty;
        public string ItemCode { get; set; } = string.Empty;
    }

    public class BulkRegisterRequest
    {
        public int CategoryId { get; set; }
        public int? LocationId { get; set; }
        public int? DepartmentId { get; set; }
        public int Count { get; set; }
    }

    public class ConfirmRegistrationRequest
    {
        public List<int> ItemIds { get; set; } = new();
    }
}
