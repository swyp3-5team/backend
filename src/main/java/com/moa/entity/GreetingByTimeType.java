package com.moa.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시간대별 인사 리스트
 */
@Getter
@RequiredArgsConstructor
public enum GreetingByTimeType {

    MORNING_GREETINGS(new String[] {
            "좋은 아침이에요!",
            "오늘도 활기찬 하루 보내세요!",
            "상쾌한 아침이네요~",
            "아침 식사는 하셨나요?",
            "일찍 일어나셨네요!"
    }),

    AFTERNOON_GREETINGS(new String[] {
            "맛있는 점심 드셨나요?",
            "오후도 힘내세요!",
            "점심 후 나른하시죠?",
            "오늘 오후는 어떠세요?",
            "점심은 뭐 드셨어요?"
    }),

    EVENING_GREETINGS(new String[] {
            "오늘 하루 수고했어요!",
            "저녁 식사 시간이네요~",
            "하루 마무리 잘하세요!",
            "피곤하시죠? 조금만 더 힘내요!",
            "오늘도 고생 많으셨어요!"
    }),

    NIGHT_GREETINGS(new String[] {
            "아직 안 주무셨군요~",
            "늦은 시간이에요, 푹 쉬세요!",
            "내일을 위해 일찍 주무세요!",
            "오늘 하루도 수고하셨어요!",
            "좋은 꿈 꾸세요~"
    });

    private final String[] greetingList;
}
