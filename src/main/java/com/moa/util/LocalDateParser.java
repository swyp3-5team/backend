package com.moa.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LocalDateParser {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,              // 2025-01-02
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private LocalDateParser() {}

    public static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate parsed = LocalDate.parse(value, formatter);

                if (parsed.getYear() == 0) {
                    return parsed.withYear(LocalDate.now().getYear());
                }

                return parsed;

            } catch (DateTimeParseException ignored) {
            }
        }

        return LocalDate.now();
        // (원하신다면 기존처럼 throw new IllegalArgumentException(...)을 쓰셔도 됩니다.)
    }
}
