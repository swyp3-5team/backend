package com.moa.entity;

import lombok.Getter;

@Getter
public enum TransactionEmotion {

    NEUTRAL("무심하게 소비"),        // 일상
    STRESS_RELIEF("스트레스를 풀기 위해 소비"),  // 스트레스 해소
    REWARD("보상 심리로 소비"),         // 보상 심리
    IMPULSE("충동적으로 소비"),        // 충동 구매
    REGRET("후회하는 소비"),         // 후회
    SATISFACTION("만족스러운 소비");    // 만족

    private final String naturalString;

    TransactionEmotion(String naturalString) {
        this.naturalString = naturalString;
    }
    /**
     * Emotion 문자열을 TransactionEmotion으로 변환
     */
    public static TransactionEmotion parseEmotion(String emotion) {
        if (emotion == null || emotion.trim().isEmpty()) {
            return TransactionEmotion.NEUTRAL;  // 기본값
        }
        TransactionEmotion parsedEmotion = from(emotion);
        if (parsedEmotion != NEUTRAL) {
            return parsedEmotion;
        }

        String normalizedEmotion = emotion.trim().toLowerCase();
        // 키워드 매칭
//        if (normalizedEmotion.contains("기쁨") || normalizedEmotion.contains("행복") || normalizedEmotion.contains("즐거움") || normalizedEmotion.contains("좋음")) {
//            return TransactionEmotion.HAPPY;
//        } else if (normalizedEmotion.contains("슬픔") || normalizedEmotion.contains("우울") || normalizedEmotion.contains("아쉬움")) {
//            return TransactionEmotion.SADNESS;
        if (normalizedEmotion.contains("스트레스") || normalizedEmotion.contains("해소") || normalizedEmotion.contains("풀림") || normalizedEmotion.contains("힐링")) {
            return TransactionEmotion.STRESS_RELIEF;
        } else if (normalizedEmotion.contains("보상") || normalizedEmotion.contains("기분전환") || normalizedEmotion.contains("선물")) {
            return TransactionEmotion.REWARD;
        } else if (normalizedEmotion.contains("충동") || normalizedEmotion.contains("즉흥") || normalizedEmotion.contains("갑작스러움")) {
            return TransactionEmotion.IMPULSE;
//        } else if (normalizedEmotion.contains("계획") || normalizedEmotion.contains("목적") || normalizedEmotion.contains("예정")) {
//            return TransactionEmotion.PLANNED;
        } else if (normalizedEmotion.contains("후회") || normalizedEmotion.contains("아까움") || normalizedEmotion.contains("낭비")) {
            return TransactionEmotion.REGRET;
        } else if (normalizedEmotion.contains("만족") || normalizedEmotion.contains("충족") || normalizedEmotion.contains("행복감")) {
            return TransactionEmotion.SATISFACTION;
        } else {
            return TransactionEmotion.NEUTRAL;
        }
    }

    public static TransactionEmotion from(String emotion) {
        if (emotion == null || emotion.isBlank()) {
            return NEUTRAL;
        }

        for (TransactionEmotion value : values()) {
            if (value.name().equalsIgnoreCase(emotion.trim())) {
                return value;
            }
        }

        return NEUTRAL;
    }

}

