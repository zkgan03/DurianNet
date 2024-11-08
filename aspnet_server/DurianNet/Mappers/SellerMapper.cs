using DurianNet.Dtos.Request.Seller;
using DurianNet.Dtos.Response;
using DurianNet.Models.DataModels;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.Net.NetworkInformation;
using static DurianNet.Dtos.Response.SellerDtoResponse;

namespace DurianNet.Mappers
{
    public static class SellerMapper
    {
        public static SellerDtoResponse ToSellerDtoResponse(this Seller seller)
        {

            return new SellerDtoResponse
            {
                SellerId = seller.SellerId,
                Name = seller.Name,
                Description = seller.Description,
                ImageUrl = seller.Image,
                Rating = seller.Comments.Count == 0 ? 0 : seller.Comments.Average(c => c.Rating),
                Latitude = seller.Latitude,
                Longitude = seller.Longitude,
                DurianTypes = seller.DurianProfiles.Select(durian => new DurianTypeDto
                {
                    DurianId = durian.DurianId,
                    Name = durian.DurianName
                }).ToList(),

                User = new SellerUserDto
                {
                    UserId = seller.User.Id,
                    Username = seller.User.UserName
                }
            };
        }

        public static Seller ToSellerFromAdd(this AddSellerDtoRequest dto)
        {
            return new Seller
            {
                UserId = dto.UserId,
                Name = dto.Name,
                Description = dto.Description,
                Image = dto.Image,
                Latitude = dto.Latitude,
                Longitude = dto.Longitude,
                DurianProfiles = dto.DurianProfileId.Select(durian => new DurianProfile { DurianId = durian }).ToList()
            };
        }

        public static Seller ToSellerFromUpdate(this UpdateSellerDtoRequest dto)
        {
            return new Seller
            {
                Name = dto.Name,
                Description = dto.Description,
                DurianProfiles = dto.DurianProfileId.Select(durian => new DurianProfile { DurianId = durian }).ToList()
            };
        }
    }

}
