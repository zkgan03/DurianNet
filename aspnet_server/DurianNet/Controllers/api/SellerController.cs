using DurianNet.Dtos.Request.Seller;
using DurianNet.Dtos.Response;
using DurianNet.Mappers;
using DurianNet.Models.DataModels;
using DurianNet.Services.CommentService;
using DurianNet.Services.SellerService;
using DurianNet.Utils;
using Microsoft.AspNetCore.Mvc;
using System.Text.Json;

namespace DurianNet.Controllers.api
{
    [Route("api/[controller]")]
    [ApiController]
    public class SellerController : ControllerBase
    {

        private readonly ISellerService _sellerService;
        private readonly ICommentService _commentService;

        public SellerController(ISellerService sellerService, ICommentService commentService)
        {
            _sellerService = sellerService;
            _commentService = commentService;
        }


        [HttpPost("AddSeller")]
        public async Task<IActionResult> AddSeller([FromBody] AddSellerDtoRequest request)
        {
            Console.WriteLine("AddSeller called");

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var addedSeller = await _sellerService.AddSellerAsync(request.ToSellerFromAdd());

            return Ok(addedSeller.ToSellerDtoResponse());
        }


        [HttpGet("GetSellers")]
        public async Task<IActionResult> GetAllSellers()
        {
            Console.WriteLine("GetAllSellers called");

            List<Seller> sellers = await _sellerService.GetAllSellersAsync();

            //remove all images from sellers for testing
            //sellers.ForEach(s => s.Image = "n");


            List<SellerDtoResponse> sellerResponseDtos = sellers.Select(s => s.ToSellerDtoResponse()).ToList();

            //Console.WriteLine(JsonSerializer.Serialize(sellerResponseDtos[0]));

            return Ok(sellerResponseDtos);
        }

        [HttpGet("GetSellers/{sellerId}")]
        public async Task<IActionResult> GetSellerById([FromRoute] int sellerId)
        {
            Console.WriteLine("GetSellerById called");

            Seller seller = await _sellerService.GetSellerByIdAsync(sellerId);

            SellerDtoResponse response = seller.ToSellerDtoResponse();

            return Ok(response);
        }

        [HttpGet("GetSellersAddedByUser/{userId}")]
        public async Task<IActionResult> GetSellersAddedByUser([FromRoute] string userId)
        {
            Console.WriteLine("GetSellersAddedByUser called");

            //TODO: get the user id from the token (authentication) instead of the route

            List<Seller> sellers = await _sellerService.GetSellersAddedByUserAsync(userId);

            List<SellerDtoResponse> sellerResponseDtos = sellers.Select(s => s.ToSellerDtoResponse()).ToList();

            return Ok(sellerResponseDtos);
        }

        [HttpGet("SearchSellers")]
        public async Task<IActionResult> SearchSellers([FromQuery] SearchSellersRequest searchSellersRequest)
        {
            Console.WriteLine("SearchSeller called");

            List<Seller> searchResult = await _sellerService.SearchSellerByNameAsync(searchSellersRequest.query);

            var response = searchResult.Select(s => s.ToSellerDtoResponse()).ToList();

            return Ok(response);
        }


        [HttpPut("UpdateSeller/{sellerId}")]
        public async Task<IActionResult> UpdateSeller([FromRoute] int sellerId, [FromBody] UpdateSellerDtoRequest request)
        {
            Console.WriteLine("UpdateSeller called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            Console.WriteLine(JsonSerializer.Serialize(request.ToSellerFromUpdate()));

            var seller = await _sellerService.GetSellerByIdAsync(sellerId);
            if (seller == null) return BadRequest(ModelState);

            // TODO: get the user id from the token (authentication)

            var updatedSeller = await _sellerService.UpdateSellerAsync(sellerId, request.ToSellerFromUpdate());

            return Ok(updatedSeller.ToSellerDtoResponse());
        }

        [HttpDelete("RemoveSeller/{sellerId}")]
        public async Task<IActionResult> RemoveSeller([FromRoute] int sellerId)
        {
            Console.WriteLine("RemoveSeller called");

            if (!ModelState.IsValid) return BadRequest(ModelState);

            var seller = await _sellerService.GetSellerByIdAsync(sellerId);
            if (seller == null) return BadRequest(ModelState);

            // TODO: get the user id from the token (authentication)

            await _sellerService.RemoveSellerAsync(sellerId);

            return Ok();
        }

    }
}
