using Beetech.Tms.Core.Models;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace Beetech.Tms.Core.Data;

public class TmsDbContext : IdentityDbContext<AppUser, AppRole, int>
{
    public TmsDbContext(DbContextOptions<TmsDbContext> options) : base(options)
    {
    }

    public DbSet<Category> Categories { get; set; }
    public DbSet<Location> Locations { get; set; }
    public DbSet<Department> Departments { get; set; }
    public DbSet<TextileItem> TextileItems { get; set; }
    public DbSet<Transaction> Transactions { get; set; }
    public DbSet<TransactionItem> TransactionItems { get; set; }
    public DbSet<AuditLog> AuditLogs { get; set; }

    public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        foreach (var entry in ChangeTracker.Entries<ISoftDelete>())
        {
            switch (entry.State)
            {
                case EntityState.Deleted:
                    entry.State = EntityState.Modified;
                    entry.Entity.IsDeleted = true;
                    entry.Entity.DeletedAt = DateTime.UtcNow;
                    break;
            }
        }

        foreach (var entry in ChangeTracker.Entries<IAuditable>())
        {
            switch (entry.State)
            {
                case EntityState.Added:
                    entry.Entity.CreatedAt = DateTime.UtcNow;
                    break;
                case EntityState.Modified:
                    entry.Entity.UpdatedAt = DateTime.UtcNow;
                    break;
            }
        }

        return await base.SaveChangesAsync(cancellationToken);
    }

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);

        // Global Query Filters
        builder.Entity<Category>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<Location>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<Department>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<TextileItem>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<Transaction>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<TransactionItem>().HasQueryFilter(e => !e.IsDeleted);
        builder.Entity<AppUser>().HasQueryFilter(e => !e.IsDeleted);

        // Identity table names
        builder.Entity<AppUser>(entity => { entity.ToTable(name: "Users"); });
        builder.Entity<AppRole>(entity => { entity.ToTable(name: "Roles"); });
        builder.Entity<Microsoft.AspNetCore.Identity.IdentityUserRole<int>>(entity => { entity.ToTable("UserRoles"); });
        builder.Entity<Microsoft.AspNetCore.Identity.IdentityUserClaim<int>>(entity => { entity.ToTable("UserClaims"); });
        builder.Entity<Microsoft.AspNetCore.Identity.IdentityUserLogin<int>>(entity => { entity.ToTable("UserLogins"); });
        builder.Entity<Microsoft.AspNetCore.Identity.IdentityRoleClaim<int>>(entity => { entity.ToTable("RoleClaims"); });
        builder.Entity<Microsoft.AspNetCore.Identity.IdentityUserToken<int>>(entity => { entity.ToTable("UserTokens"); });

    }
}
