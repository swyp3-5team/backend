package com.moa.entity.conf;

import lombok.Getter;

@Getter
public enum AiChatEnum {
    EMPATH("EMPATH"),      // 공감형
    FACT("FACT")       // 팩폭형
    ;

    private final String text;

    AiChatEnum(String text) {
        this.text = text;
    }
}
