package com.moa.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public record ClovaOcrRequest(
        String version,
        String requestId,
        long timestamp,
        String lang,
        List<OcrImage> images
) {
    public ClovaOcrRequest {
        Objects.requireNonNull(version);
        Objects.requireNonNull(requestId);
        Objects.requireNonNull(lang);
        Objects.requireNonNull(images);

        if (images.isEmpty()) {
            throw new IllegalArgumentException("images must not be empty");
        }
    }

    public static ClovaOcrRequest from(String requestId, MultipartFile file) throws IOException {
        return new ClovaOcrRequest(
                "V2",
                requestId,
                System.currentTimeMillis(),
                "ko",
                List.of(new OcrImage(
                        getFormat(file.getOriginalFilename()),
                        file.getOriginalFilename(),
                        Base64.getEncoder().encodeToString(file.getBytes())
                ))
        );
    }

    private static String getFormat(String filename) {
        int idx = filename.lastIndexOf('.');
        return filename.substring(idx + 1).toLowerCase();
    }

    public record OcrImage(
            String format,
            String name,
            String data
    ) {}
}
