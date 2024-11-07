using DurianNet.Dtos;
using DurianNet.Dtos.Response;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using DurianNet.Services.SellerService;
using DurianNet.Utils;
using Microsoft.AspNetCore.Mvc;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class SellerLocatorController : ControllerBase
    {

        private readonly ISellerService _sellerService;

        public SellerLocatorController(ISellerService sellerService)
        {
            _sellerService = sellerService;
        }


        [HttpGet("GetAllSellers")]
        public async Task<IActionResult> GetAllSellers()
        {
            Console.WriteLine("GetAllSellers called");

            List<Seller> sellers = await _sellerService.GetAllSellersAsync();

            List<SellerDtoResponse> sellerResponseDtos = sellers.Select(s => s.ToSellerDtoResponse()).ToList();

            return Ok(sellerResponseDtos);
        }


        [HttpGet("SearchSellers")]
        public async Task<IActionResult> SearchSellers([FromQuery] SearchSellersRequest searchSellersRequest)
        {
            Console.WriteLine("SearchSeller called");

            List<Seller> searchResult = await _sellerService.GetAllSellersAsync();


            var filteredList = searchResult
                    .Where(p => p.Name.ToLower().Contains(searchSellersRequest.query.ToLower()))
                    .ToList();

            //sort according to relevance (number of matching characters)
            filteredList.Sort((a, b) =>
                b.Name.Count(c => searchSellersRequest.query.Contains(c))
                    .CompareTo(a.Name.Count(c => searchSellersRequest.query.Contains(c))));

            var response = filteredList.Select(s => s.ToSellerDtoResponse()).ToList();

            return Ok(response);
        }


        [HttpGet("GetSellerComments/{sellerId}")]
        public IActionResult GetSellerComments([FromRoute] int sellerId)
        {
            Console.WriteLine("GetComments called");

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var seller = FakeAppData
                .dummySellers
                .FirstOrDefault(s => s.SellerId == sellerId);

            if (seller == null)
            {
                return BadRequest(ModelState);
            }

            return Ok(seller.Comments);
        }

    }
}
