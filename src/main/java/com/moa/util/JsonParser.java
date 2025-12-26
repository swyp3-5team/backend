package com.moa.util;

public class JsonParser {
    public static String cleanUpJson(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new IllegalArgumentException("AI content is empty");
        }

        // ```json ... ``` 제거
        String cleaned = rawContent
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start == -1 || end == -1 || start > end) {
            throw new IllegalArgumentException(
                    "JSON object not found in AI content"
            );
        }

        return cleaned.substring(start, end + 1);
    }
    public static Long parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return null;
        }

        String cleanedAmount = amount.replaceAll("[^0-9]", "");
        return Long.parseLong(cleanedAmount);
    }
}
