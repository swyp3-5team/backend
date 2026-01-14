package com.moa.dto;

import java.util.List;

public record UpstageOcrResponse(
        String apiVersion,
        Double confidence,
        Metadata metadata,
        String mimeType,
        String modelVersion,
        Integer numBilledPages,
        List<Page> pages,
        Boolean stored,
        String text
) {
    public record Metadata(
            List<MetaPage> pages
    ) {}

    public record MetaPage(
            Integer height,
            Integer page,
            Integer width
    ) {}

    public record Page(
            Double confidence,
            Integer height,
            Integer id,
            String text,
            Integer width,
            List<Word> words
    ) {}

    public record Word(
            BoundingBox boundingBox,
            Double confidence,
            Integer id,
            String text
    ) {}

    public record BoundingBox(
            List<Vertex> vertices
    ) {}

    public record Vertex(
            Double x,
            Double y
    ) {}
}
