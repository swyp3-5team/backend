package com.moa.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 캐릭터 감정 타입
 */
@Getter
@RequiredArgsConstructor
public enum CharacterEmotionType {
    BASIC("😊"),
    HAPPY("🥰"),
    CHEER("💪"),
    COMFORT("🥺");

    private final String emoji;
}
