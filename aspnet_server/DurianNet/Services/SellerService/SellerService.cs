using DurianNet.Data;
using DurianNet.Exceptions;
using DurianNet.Models.DataModels;
using DurianNet.Utils;
using Microsoft.EntityFrameworkCore;
using System.Runtime.Intrinsics.Arm;
using System.Text.Json;

namespace DurianNet.Services.SellerService
{
    public class SellerService : ISellerService
    {

        private readonly ApplicationDBContext _context;
        private readonly IWebHostEnvironment _environment;

        public SellerService(ApplicationDBContext context, IWebHostEnvironment environment)
        {
            _context = context;
            _environment = environment;
        }


        /// <summary>
        /// Add a new seller to the database
        /// Image will be saved in wwwroot/images
        /// </summary>
        /// <param name="seller"></param>
        /// <returns>
        /// The added seller with Id will be returned
        /// </returns>
        public async Task<Seller> AddSellerAsync(Seller seller)
        {
            var durianProfileIds = seller.DurianProfiles.Select(dp => dp.DurianId).ToList();

            List<DurianProfile> durianProfiles = await _GetDurianProfilesAsync(durianProfileIds);
            seller.DurianProfiles = durianProfiles;

            //decode the seller image and save into wwwroot/images
            var wwwrootPath = _environment.WebRootPath;
            var imagePath = Path.Combine(wwwrootPath, "images", $"{Guid.NewGuid()}.jpg");
            ImageUtils.SaveImage(seller.Image, imagePath);

            //save relative path to database
            seller.Image = imagePath.Replace(wwwrootPath + "\\", "").Replace("\\", "/");

            await _context.Sellers.AddAsync(seller);
            await _context.SaveChangesAsync();

            _context.Entry(seller).Reference(s => s.User).Load();
            _context.Entry(seller).Collection(s => s.DurianProfiles).Load();
            _context.Entry(seller).Collection(s => s.Comments).Load();

            //return added seller
            return seller;
        }


        /// <summary>
        /// Get all sellers, the deleted sellers will be omitted
        /// </summary>
        /// <returns></returns>
        public async Task<List<Seller>> GetAllSellersAsync()
        {
            var sellers = await _context
                .Sellers
                .Where(s => s.IsDeleted == false)
                .Include(s => s.DurianProfiles)
                .Include(s => s.Comments)
                .Include(s => s.User)
                .ToListAsync();

            return sellers;
        }


        /// <summary>
        /// Get a seller by Id
        /// Deleted seller will be returned also
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        /// <exception cref="DataNotFoundException"></exception>
        public async Task<Seller> GetSellerByIdAsync(int id)
        {
            var seller = await _context.Sellers
                .Where(s => s.SellerId == id)
                .Include(s => s.DurianProfiles)
                .Include(s => s.Comments)
                .Include(s => s.User)
                .FirstOrDefaultAsync();

            if (seller == null)
            {
                throw new DataNotFoundException("Seller Not Found");
            }

            return seller;
        }


        /// <summary>
        /// Get all sellers added by a user
        /// Deleted sellers will be omitted
        /// </summary>
        /// <param name="userId"></param>
        /// <returns></returns>
        public async Task<List<Seller>> GetSellersAddedByUserAsync(string userId)
        {
            var seller = await _context.Sellers
                .Where(s => s.UserId == userId && s.IsDeleted == false)
                .Include(s => s.DurianProfiles)
                .Include(s => s.Comments)
                .Include(s => s.User)
                .ToListAsync();

            return seller;
        }


        /// <summary>
        /// Remove a seller from the database by given Id
        /// IsDeleted will be set to true
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        /// <exception cref="DataNotFoundException"></exception>
        public async Task RemoveSellerAsync(int id)
        {
            var seller = await _context.Sellers.FindAsync(id);

            if (seller == null)
            {
                throw new DataNotFoundException("Seller Not Found");
            }

            seller.IsDeleted = true;

            //_context.Sellers.Remove(seller);
            await _context.SaveChangesAsync();

        }

        /// <summary>
        /// Search sellers by name
        /// Deleted sellers will be omitted
        /// </summary>
        /// <param name="name"></param>
        /// <returns></returns>
        public async Task<List<Seller>> SearchSellerByNameAsync(string name)
        {
            var sellers = await _context.Sellers
                .Where(s => s.Name.ToLower().Contains(name.ToLower()) && s.IsDeleted == false)
                .Include(s => s.DurianProfiles)
                .Include(s => s.Comments)
                .Include(s => s.User)
                .ToListAsync();

            var sortedSellers = sellers
                .OrderByDescending(s => s.Name.ToLower().Split(new[] { name.ToLower() }, StringSplitOptions.None).Length - 1)
                .ToList();

            return sortedSellers;
        }


        /// <summary>
        /// Update a seller by given Id and new seller given
        /// </summary>
        /// <param name="id"></param>
        /// <param name="newSeller"></param>
        /// <returns></returns>
        /// <exception cref="DataNotFoundException"></exception>
        public async Task<Seller> UpdateSellerAsync(int id, Seller newSeller)
        {
            var seller = await _context.Sellers
                .Include(s => s.DurianProfiles)
                .FirstOrDefaultAsync(s => s.SellerId == id);

            if (seller == null)
            {
                throw new DataNotFoundException("Seller Not Found");
            }

            seller.Name = newSeller.Name;
            seller.Description = newSeller.Description;

            var newDurianProfileIds = newSeller.DurianProfiles.Select(dp => dp.DurianId).ToList();
            List<DurianProfile> durianProfiles = await _GetDurianProfilesAsync(newDurianProfileIds);
            seller.DurianProfiles = durianProfiles;

            await _context.SaveChangesAsync();

            _context.Entry(seller).Reference(s => s.User).Load();

            return seller;
        }


        private async Task<List<DurianProfile>> _GetDurianProfilesAsync(List<int> durianProfileIds)
        {
            //get collection of durian profiles from id
            return await _context.DurianProfiles
                .Where(dp => durianProfileIds.Contains(dp.DurianId))
                .ToListAsync();
        }

    }
}
