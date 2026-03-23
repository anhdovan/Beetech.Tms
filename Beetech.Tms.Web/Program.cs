using Beetech.Tms.Core.Data;
using Beetech.Tms.Core.Models;
using Beetech.Tms.Web.Services;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Radzen;
using Beetech.Tms.Web.Components;
using Serilog;
using OfficeOpenXml;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using Microsoft.AspNetCore.Localization;
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);

ExcelPackage.License.SetNonCommercialPersonal("Beetech TMS");

builder.Host.UseSerilog((context, services, configuration) => configuration
    .ReadFrom.Configuration(context.Configuration)
    .ReadFrom.Services(services)
    .Enrich.FromLogContext());

// Add services to the container.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

builder.Services.AddLocalization(options => options.ResourcesPath = "Resources");

builder.Services.AddControllers();

// Add Radzen services
builder.Services.AddRadzenComponents();

// Add Database
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") ?? "Data Source=tms.db";
builder.Services.AddDbContext<TmsDbContext>(options =>
    options.UseSqlite(connectionString));

// Add Identity
builder.Services.AddIdentity<AppUser, AppRole>(options => {
    options.SignIn.RequireConfirmedAccount = false;
    options.Password.RequireDigit = false;
    options.Password.RequiredLength = 6;
    options.Password.RequireNonAlphanumeric = false;
    options.Password.RequireUppercase = false;
    options.Password.RequireLowercase = false;
})
    .AddEntityFrameworkStores<TmsDbContext>()
    .AddDefaultTokenProviders();

// Add JWT Authentication (in addition to Identity cookies)
builder.Services.AddAuthentication()
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"] ?? "BeetechTMS",
            ValidAudience = builder.Configuration["Jwt:Audience"] ?? "BeetechTMS",
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Key"] ?? "this_is_a_very_secret_key_for_jwt_32_chars_long"))
        };
    });

builder.Services.AddCascadingAuthenticationState();
builder.Services.AddAuthorization();

// Custom services
builder.Services.AddScoped<TmsService>();

var app = builder.Build();

// Ensure database is created
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    var db = services.GetRequiredService<TmsDbContext>();
    var userManager = services.GetRequiredService<UserManager<AppUser>>();
    var roleManager = services.GetRequiredService<RoleManager<AppRole>>();
    
    db.Database.EnsureCreated();
    var seeder = new DataSeeder(db, userManager, roleManager);
    await seeder.SeedAsync();
}

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

var supportedCultures = new[] { "en-US", "vi-VN" };
var localizationOptions = new RequestLocalizationOptions()
    .SetDefaultCulture(supportedCultures[0])
    .AddSupportedCultures(supportedCultures)
    .AddSupportedUICultures(supportedCultures);

app.UseRequestLocalization(localizationOptions);
app.UseAntiforgery();

app.UseAuthentication();
app.UseAuthorization();





app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.MapControllers();

app.Run();
