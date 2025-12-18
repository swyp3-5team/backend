package com.moa.entity;

public enum TransactionEmotion {

    STRESS_RELIEF("🥺", "마음 치료비"),
    REWARD("🥰", "나를 위한 선물"),
    IMPULSE("🤔", "순간의 유혹"),
    PLANNED("😊", "현명한 소비"),
    REGRET("😔", "배움의 비용"),
    SATISFACTION("🥳", "행복 충전"),
    NEUTRAL("😐", "무난한 지출");

    private final String emoji;
    private final String description;

    TransactionEmotion(String emoji, String description) {
        this.emoji = emoji;
        this.description = description;
    }

    public static TransactionEmotion from(String emotion) {
        return TransactionEmotion.valueOf(emotion);
    }

    public String emoji() {
        return emoji;
    }

    public String description() {
        return description;
    }
}

