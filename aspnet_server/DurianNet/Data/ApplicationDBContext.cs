using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Data
{
    public class ApplicationDBContext : IdentityDbContext<User>
    {

        public ApplicationDBContext(DbContextOptions<ApplicationDBContext> options) : base(options) { }

        public DbSet<DurianVideo> DurianVideos { get; set; }
        public DbSet<DurianProfile> DurianProfiles { get; set; }
        public DbSet<Comment> Comments { get; set; }
        public DbSet<Seller> Sellers { get; set; }
        public DbSet<FavoriteDurian> FavoriteDurians { get; set; }
        public DbSet<RefreshToken> RefreshTokens { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<FavoriteDurian>(x => x.HasKey(p => new { p.UserId, p.DurianId }));

            modelBuilder.Entity<FavoriteDurian>()
                .HasOne(u => u.User)
                .WithMany(u => u.FavoriteDurians)
                .HasForeignKey(p => p.UserId);

            modelBuilder.Entity<FavoriteDurian>()
                .HasOne(u => u.DurianProfile)
                .WithMany(u => u.FavoriteDurians)
                .HasForeignKey(p => p.DurianId);

            List<IdentityRole> roles = new List<IdentityRole>
            {
                new IdentityRole
                {
                    Name = "Admin",
                    NormalizedName = "ADMIN"
                },
                new IdentityRole
                {
                    Name = "User",
                    NormalizedName = "USER"
                },
            };
            modelBuilder.Entity<IdentityRole>().HasData(roles);

            modelBuilder.Entity<Comment>()
                .HasOne(c => c.Seller)
                .WithMany(s => s.Comments)
                .HasForeignKey(c => c.SellerId)
                .OnDelete(DeleteBehavior.NoAction);

        }

    }
}
