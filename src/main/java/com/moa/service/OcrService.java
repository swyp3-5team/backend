package com.moa.service;

import com.moa.config.chat.ClovaStudioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    private final ClovaStudioConfig studioConfig;
    private final WebClient webClient = WebClient.builder().build();

    public String extractTransaction(MultipartFile image) throws IOException {
        ClovaOcrRequest clovaOcrRequest = ClovaOcrRequest.from(
                UUID.randomUUID().toString(),
                image
        );
        log.info("secret, url : {}, {}",studioConfig.getOcrUri(),studioConfig.getOcrSecretKey());

        ClovaOcrResponse clovaOcrResponse = webClient.post()
                .uri(studioConfig.getOcrUri().trim())
                .header("X-OCR-SECRET", studioConfig.getOcrSecretKey().trim())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(clovaOcrRequest)
                .retrieve()
                .bodyToMono(ClovaOcrResponse.class)
                .block();

        if (clovaOcrResponse == null || clovaOcrResponse.images().isEmpty()) {
            return "";
        }

        List<ClovaOcrResponse.Field> fields = clovaOcrResponse.images().get(0).fields();

        // 1. Y축 좌표를 기준으로 같은 줄인지 판단하여 정렬하는 로직
        return sortAndProcessFields(fields);
    }

    private String sortAndProcessFields(List<ClovaOcrResponse.Field> fields) {
        if (fields == null || fields.isEmpty()) return "";

        // 1. 신뢰도 필터링
        List<ClovaOcrResponse.Field> validFields = fields.stream()
                .filter(f -> f.inferConfidence() > 0.6)
                .collect(Collectors.toList());

        // 2. Center Y 기준 전체 정렬
        validFields.sort(Comparator.comparingDouble(f -> getCenterY(f.boundingPoly())));

        StringBuilder result = new StringBuilder();
        List<ClovaOcrResponse.Field> currentLine = new ArrayList<>();

        double lastCenterY = -1;
        double lastHeight = 0; // 기준 높이 변수 추가

        for (ClovaOcrResponse.Field field : validFields) {
            double currentCenterY = getCenterY(field.boundingPoly());
            double currentHeight = getHeight(field.boundingPoly());

            if (currentLine.isEmpty()) {
                // 줄의 시작: 기준점 잡기
                currentLine.add(field);
                lastCenterY = currentCenterY;
                lastHeight = currentHeight; // 이 줄의 기준 높이 설정
            } else {
                // 기준 높이(lastHeight)를 기반으로 오차 범위 계산
                double dynamicThreshold = lastHeight * 0.6;

                if (Math.abs(currentCenterY - lastCenterY) < dynamicThreshold) {
                    // 같은 줄
                    currentLine.add(field);
                } else {
                    // 새로운 줄 시작
                    processLine(result, currentLine);
                    currentLine.clear();

                    // 새 줄의 첫 요소로 초기화
                    currentLine.add(field);
                    lastCenterY = currentCenterY;
                    lastHeight = currentHeight;
                }
            }
        }
        // 마지막 줄 처리
        processLine(result, currentLine);

        return result.toString().trim();
    }

    private void processLine(StringBuilder result, List<ClovaOcrResponse.Field> line) {
        if (line.isEmpty()) return;

        // 같은 줄 내에서는 X축 순서대로 정렬
        line.sort(Comparator.comparingDouble(f -> getMinX(f.boundingPoly())));

        String lineText = line.stream()
                .map(ClovaOcrResponse.Field::inferText)
                .collect(Collectors.joining(" "));

        result.append(lineText).append("\n");
    }

    // 중심점 계산
    private double getCenterY(ClovaOcrResponse.BoundingPoly poly) {
        double minY = poly.vertices().stream().mapToDouble(v -> v.y()).min().orElse(0);
        double maxY = poly.vertices().stream().mapToDouble(v -> v.y()).max().orElse(0);
        return (minY + maxY) / 2.0;
    }

    // 필드 높이 계산
    private double getHeight(ClovaOcrResponse.BoundingPoly poly) {
        double minY = poly.vertices().stream().mapToDouble(v -> v.y()).min().orElse(0);
        double maxY = poly.vertices().stream().mapToDouble(v -> v.y()).max().orElse(0);
        return maxY - minY;
    }

    // 좌표 정보에서 최소 X값 추출
    private double getMinX(ClovaOcrResponse.BoundingPoly poly) {
        return poly.vertices().stream()
                .mapToDouble(v -> v.x())
                .min()
                .orElse(0);
    }
}
