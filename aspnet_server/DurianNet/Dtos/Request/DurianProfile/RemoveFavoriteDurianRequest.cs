﻿namespace DurianNet.Dtos.Request.DurianProfile
{
    public class RemoveFavoriteDurianRequest
    {
        public string Username { get; set; } // User's username
        public string DurianName { get; set; } // Durian's name to be removed
    }
}