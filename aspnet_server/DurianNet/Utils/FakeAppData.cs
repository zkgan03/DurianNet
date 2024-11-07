
using DurianNet.Constants;
using DurianNet.Models.DataModels;

namespace DurianNet.Utils;

public static class FakeAppData
{
    public static List<User> dummyUsers = new()
    {
        new() { Id = "1", UserName = "Alice" },
        new() { Id = "2", UserName = "Bob" },
        new() { Id = "3", UserName = "Charlie" },
        new() { Id = "4", UserName = "David" },
        new() { Id = "5", UserName = "Eve" }
    };

    public static List<Comment> dummyComments = new()
    {
        new() { CommentId = 1, Rating = 5, Content = "Great service!", User = dummyUsers[0] },
        new() { CommentId = 2, Rating = 4, Content = "Good prices.", User = dummyUsers[1] },
        new() { CommentId = 3, Rating = 4, Content = "Friendly staff.", User = dummyUsers[2] },
        new() { CommentId = 4, Rating = 5, Content = "Highly recommend.", User = dummyUsers[3] },
        new() { CommentId = 5, Rating = 4, Content = "Will visit again.", User = dummyUsers[4] }
    };

    public static List<DurianProfile> dummyDurianProfiles = new()
    {
        new()
        {
            DurianId = "1",
            DurianName = DurianType.MusangKing,
            DurianDescription = "Musang King is known for its rich flavor and creamy texture.",
            Characteristics = "Golden yellow flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly bitter, creamy",
            DurianImage = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg"))
        },
        new()
        {
            DurianId = "2",
            DurianName = DurianType.D24,
            DurianDescription = "D24 is popular for its balanced taste and smooth texture.",
            Characteristics = "Pale yellow flesh, medium seeds, mild aroma",
            TasteProfile = "Sweet, slightly bitter, smooth",
            DurianImage = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg"))
        },
        new()
        {
            DurianId = "3",
            DurianName = DurianType.RedPrawn,
            DurianDescription = "Red Prawn is famous for its reddish flesh and unique taste.",
            Characteristics = "Reddish flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly alcoholic, creamy",
            DurianImage = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg"))
        },
        new()
        {
            DurianId = "4",
            DurianName = DurianType.BlackThorn,
            DurianDescription = "Black Thorn is prized for its rich and complex flavor.",
            Characteristics = "Dark yellow flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly bitter, rich",
            DurianImage = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg"))
        }
    };

    public static List<Seller> dummySellers = new()
    {
        new()
        {
            SellerId = 1,
            Name = "Valencia Cyclery",
            Description = "Valencia Cyclery description",
            Rating = 4.2,
            Latitude = 37.7557557,
            Longitude = -122.4208508,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[0], dummyDurianProfiles[1] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] }
        },
        new()
        {
            SellerId = 2,
            Name = "San Francisco Bicycle Rentals",
            Description = "San Francisco Bicycle Rentals description",
            Rating = 4.5,
            Latitude = 37.80764569999999,
            Longitude = -122.4195251,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[2] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] }
        },
        new()
        {
            SellerId = 3,
            Name = "Mike's Bikes of San Francisco",
            Description = "Mike's Bikes of San Francisco description",
            Rating = 4,
            Latitude = 37.7757292,
            Longitude = -122.4119508,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[3], dummyDurianProfiles[0] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[4] }
        },
        new()
        {
            SellerId = 4,
            Name = "Blazing Saddles Bike Rentals & Tours",
            Description = "Blazing Saddles Bike Rentals & Tours description",
            Rating = 4.1,
            Latitude = 37.8060487,
            Longitude = -122.4206076,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[1] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] }
        },
        new()
        {
            SellerId = 5,
            Name = "Huckleberry Bicycles",
            Description = "Huckleberry Bicycles description",
            Rating = 4.7,
            Latitude = 37.7809098,
            Longitude = -122.4117142,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[2], dummyDurianProfiles[3] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] }
        },
        new()
        {
            SellerId = 6,
            Name = "American Cyclery",
            Description = "American Cyclery description",
            Rating = 4.5,
            Latitude = 37.7665228,
            Longitude = -122.4532875,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[0] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[4] }
        },
        new()
        {
            SellerId = 7,
            Name = "The New Wheel Electric Bikes",
            Description = "The New Wheel Electric Bikes description",
            Rating = 4.8,
            Latitude = 37.7390085,
            Longitude = -122.4172602,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[1], dummyDurianProfiles[2] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] }
        },
        new()
        {
            SellerId = 8,
            Name = "Box Dog Bikes",
            Description = "Box Dog Bikes description",
            Rating = 4.4,
            Latitude = 37.7681295,
            Longitude = -122.4240983,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[3] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] }
        },
        new()
        {
            SellerId = 9,
            Name = "The Bike Kitchen",
            Description = "The Bike Kitchen description",
            Rating = 4.8,
            Latitude = 37.7609808,
            Longitude = -122.4115807,
            DurianSold = new List<DurianProfile> { dummyDurianProfiles[0], dummyDurianProfiles[1] },
            Image = Common.GetBase64Image(Path.Combine("Assets", "Image", "image_1.jpg")),
            Comments = new List<Comment> { dummyComments[4] }
        }
    };
}
