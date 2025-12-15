package com.moa.dto.chat.clova;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clova Studio API 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClovaStudioResponse {

    private Status status;
    private Result result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String code;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private Message message;
        private String stopReason;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }
    }
}
