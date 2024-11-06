﻿namespace DurianNet.Services.DetectionService.YOLO.v10.Data
{
    public readonly struct SpeedResult(
       TimeSpan preprocess,
       TimeSpan inference,
       TimeSpan postprocess)
    {
        public TimeSpan Preprocess { get; } = preprocess;

        public TimeSpan Inference { get; } = inference;

        public TimeSpan Postprocess { get; } = postprocess;

        public override string ToString()
        {
            return $"Preprocess: {Preprocess.TotalSeconds},\tInference: {Inference.TotalSeconds},\tPostprocess: {Postprocess.TotalSeconds}";
        }
    }
}
