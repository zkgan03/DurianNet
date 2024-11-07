﻿using DurianNet.Services.DetectionService.YOLO.v10.Data;
using System.Diagnostics;

namespace DurianNet.Services.DetectionService.YOLO.v10.Timer
{
    public class SpeedTimer
    {
        private readonly Stopwatch _stopwatch = new();

        private TimeSpan _preprocess;
        private TimeSpan _inference;
        private TimeSpan _postprocess;

        public TimeSpan Preprocess => _preprocess;

        public TimeSpan Inference => _inference;

        public TimeSpan Postprocess => _postprocess;

        public void StartPreprocess()
        {
            _stopwatch.Restart();
        }

        public void StartInference()
        {
            _preprocess = _stopwatch.Elapsed;
            _stopwatch.Restart();
        }

        public void StartPostprocess()
        {
            _inference = _stopwatch.Elapsed;
            _stopwatch.Restart();
        }

        public SpeedResult Stop()
        {
            _postprocess = _stopwatch.Elapsed;
            _stopwatch.Stop();

            return new SpeedResult(_preprocess,
                                   _inference,
                                   _postprocess);
        }
    }
}
