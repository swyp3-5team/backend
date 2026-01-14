package com.moa.controller.chat;

import com.moa.service.OcrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ocr")
@Tag(name = "OCR", description = "OCR TEST API")
public class OcrController {
    private final OcrService ocrService;

    @PostMapping(path = "/clova", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> extractTransaction(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok().body(ocrService.extractTransaction(file));
    }

    @PostMapping(path = "/upstage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> UpstageOcrTest(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok().body(ocrService.upstageOcr(file));
    }

}
