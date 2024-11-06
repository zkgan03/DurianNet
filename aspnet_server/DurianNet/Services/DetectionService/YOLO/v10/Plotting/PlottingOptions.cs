﻿using SixLabors.Fonts;

namespace DurianNet.Services.DetectionService.YOLO.v10.Plotting
{
    public abstract class PlottingOptions
    {
        public FontFamily FontFamily { get; set; }

        public float FontSize { get; set; }

        public PlottingOptions()
        {
            FontFamily = GetDefaultFontFamily();
            FontSize = 12F;
        }

        private static FontFamily GetDefaultFontFamily()
        {
            if (OperatingSystem.IsWindows() && SystemFonts.TryGet("Microsoft YaHei", out FontFamily family))
            {
                return family;
            }

            if (OperatingSystem.IsAndroid() && SystemFonts.TryGet("Robot", out family))
            {
                return family;
            }

            return SystemFonts.Families.FirstOrDefault();
        }
    }
}
