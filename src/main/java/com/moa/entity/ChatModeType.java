package com.moa.entity;

import lombok.Getter;

@Getter
public enum ChatModeType {
    RECEIPT("RECEIPT"), // 내역모드
    CHAT("CHAT"); // 채팅모드

    private final String text;

    ChatModeType(String text) {
        this.text = text;
    }
}