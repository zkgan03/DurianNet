﻿using DurianNet.Constants;
using DurianNet.Models.DataModels;

namespace DurianNet.Data
{
    public static class DbInitializer
    {
        public static void Seed(ApplicationDBContext context)
        {
            if (!context.Users.Any())
            {
                context.Users.AddRange(dummyUsers);
            }

            if (!context.Comments.Any())
            {
                context.Comments.AddRange(dummyComments);
            }

            if (!context.DurianProfiles.Any())
            {
                context.DurianProfiles.AddRange(dummyDurianProfiles);
            }

            if (!context.Sellers.Any())
            {
                context.Sellers.AddRange(dummySellers);
            }

            context.SaveChanges();
        }

        public static List<User> dummyUsers = new()
    {
        new() { Id = "1", UserName = "Alice", ProfilePicture = "images/image_1.jpg" },
        new() { Id = "2", UserName = "Bob", ProfilePicture = "images/image_1.jpg" },
        new() { Id = "3", UserName = "Charlie" , ProfilePicture = "images/image_1.jpg"},
        new() { Id = "4", UserName = "David", ProfilePicture = "images/image_1.jpg" },
        new() { Id = "5", UserName = "Eve" , ProfilePicture = "images/image_1.jpg"}
    };

        public static List<Comment> dummyComments = new()
    {
        new() {Rating = 5, Content = "Great service!", User = dummyUsers[0] },
        new() { Rating = 4, Content = "Good prices.", User = dummyUsers[1] },
        new() { Rating = 4, Content = "Friendly staff.", User = dummyUsers[2] },
        new() {   Rating = 5, Content = "Highly recommend.", User = dummyUsers[3] },
        new() {   Rating = 4, Content = "Will visit again.", User = dummyUsers[4] }
    };

        public static DurianVideo dummyDurianVideo = new()
        {
            VideoUrl = "https://www.youtube.com",
            Description = "Durian tasting video"
        };


        public static List<DurianProfile> dummyDurianProfiles = new()
    {
        new()
        {
            DurianName = DurianType.MusangKing,
            DurianDescription = "Musang King is known for its rich flavor and creamy texture.",
            Characteristics = "Golden yellow flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly bitter, creamy",
            DurianImage = "images/image_1.jpg",
            DurianVideo = dummyDurianVideo
        },
        new()
        {
            DurianName = DurianType.D24,
            DurianDescription = "D24 is popular for its balanced taste and smooth texture.",
            Characteristics = "Pale yellow flesh, medium seeds, mild aroma",
            TasteProfile = "Sweet, slightly bitter, smooth",
            DurianImage = "images/image_1.jpg",
            DurianVideo = dummyDurianVideo
        },
        new()
        {
            DurianName = DurianType.RedPrawn,
            DurianDescription = "Red Prawn is famous for its reddish flesh and unique taste.",
            Characteristics = "Reddish flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly alcoholic, creamy",
            DurianImage = "images/image_1.jpg",
            DurianVideo = dummyDurianVideo
        },
        new()
        {
            DurianName = DurianType.BlackThorn,
            DurianDescription = "Black Thorn is prized for its rich and complex flavor.",
            Characteristics = "Dark yellow flesh, small seeds, strong aroma",
            TasteProfile = "Sweet, slightly bitter, rich",
            DurianImage = "images/image_1.jpg",
            DurianVideo = dummyDurianVideo
        }
    };

        public static List<Seller> dummySellers = new()
    {
        new()
        {
            Name = "Valencia Cyclery",
            Description = "Valencia Cyclery description",
            Latitude = 37.7557557,
            Longitude = -122.4208508,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[0], dummyDurianProfiles[1] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] },
            User = dummyUsers[0]
        },
        new()
        {
            Name = "San Francisco Bicycle Rentals",
            Description = "San Francisco Bicycle Rentals description",
            Latitude = 37.80764569999999,
            Longitude = -122.4195251,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[2] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] },
            User = dummyUsers[1]
        },
        new()
        {
            Name = "Mike's Bikes of San Francisco",
            Description = "Mike's Bikes of San Francisco description",
            Latitude = 37.7757292,
            Longitude = -122.4119508,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[3], dummyDurianProfiles[0] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[4] },
            User = dummyUsers[2]
        },
        new()
        {
            Name = "Blazing Saddles Bike Rentals & Tours",
            Description = "Blazing Saddles Bike Rentals & Tours description",
            Latitude = 37.8060487,
            Longitude = -122.4206076,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[1] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] },
            User = dummyUsers[3]
        },
        new()
        {
            Name = "Huckleberry Bicycles",
            Description = "Huckleberry Bicycles description",
            Latitude = 37.7809098,
            Longitude = -122.4117142,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[2], dummyDurianProfiles[3] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] },
            User = dummyUsers[4]
        },
        new()
        {
            Name = "American Cyclery",
            Description = "American Cyclery description",
            Latitude = 37.7665228,
            Longitude = -122.4532875,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[0] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[4] },
            User = dummyUsers[0]
        },
        new()
        {
            Name = "The New Wheel Electric Bikes",
            Description = "The New Wheel Electric Bikes description",
            Latitude = 37.7390085,
            Longitude = -122.4172602,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[1], dummyDurianProfiles[2] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[0], dummyComments[1] },
            User = dummyUsers[1]
        },
        new()
        {
            Name = "Box Dog Bikes",
            Description = "Box Dog Bikes description",
            Latitude = 37.7681295,
            Longitude = -122.4240983,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[3] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[2], dummyComments[3] },
            User = dummyUsers[2]
        },
        new()
        {
            Name = "The Bike Kitchen",
            Description = "The Bike Kitchen description",
            Latitude = 37.7609808,
            Longitude = -122.4115807,
            DurianProfiles = new List<DurianProfile> { dummyDurianProfiles[0], dummyDurianProfiles[1] },
            Image = "images/image_1.jpg",
            Comments = new List<Comment> { dummyComments[4] },
            User = dummyUsers[3]
        }
    };
    }


}