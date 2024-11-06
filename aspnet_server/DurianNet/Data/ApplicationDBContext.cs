using DurianNet.Models.DataModels;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace DurianNet.Data
{
    public class ApplicationDBContext : IdentityDbContext<User>
    {

        public ApplicationDBContext(DbContextOptions options) : base(options)
        {
        }

        public DbSet<DurianVideo> DurianVideos { get; set; }
        public DbSet<DurianProfile> DurianProfiles { get; set; }
        public DbSet<Comment> Comments { get; set; }
        public DbSet<Seller> Sellers { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
        }

    }
}
