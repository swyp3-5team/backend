package com.moa.service;

import java.util.List;

public record ClovaOcrResponse(
        String version,
        String requestId,
        long timestamp,
        List<ImageResult> images
) {

    public record ImageResult(
            String uid,
            String name,
            String inferResult,
            String message,
            ValidationResult validationResult,
            ConvertedImageInfo convertedImageInfo,
            List<Field> fields
    ) {}

    public record ValidationResult(
            String result
    ) {}

    public record ConvertedImageInfo(
            int width,
            int height,
            int pageIndex,
            boolean longImage
    ) {}

    public record Field(
            String valueType,
            String inferText,
            double inferConfidence,
            String type,
            boolean lineBreak,
            BoundingPoly boundingPoly
    ) {}

    public record BoundingPoly(
            List<Vertex> vertices
    ) {}

    public record Vertex(
            double x,
            double y
    ) {}
}