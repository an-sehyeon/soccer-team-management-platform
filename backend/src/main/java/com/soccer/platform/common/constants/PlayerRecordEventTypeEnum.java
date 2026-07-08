package com.soccer.platform.common.constants;

// 선수 기록 이벤트 유형 ENUM
public enum PlayerRecordEventTypeEnum {

    // 득점
    GOAL,
    // 도움
    ASSIST,
    // 슈팅
    SHOT,
    // 유효 슈팅
    SHOT_ON_TARGET,
    // 패스
    PASS,
    // 성공 패스
    SUCCESSFUL_PASS,
    // 드리블
    DRIBBLE,
    // 성공 드리블
    SUCCESSFUL_DRIBBLE,
    // 태클
    TACKLE,
    // 인터셉트
    INTERCEPTION,
    // 클리어링
    CLEARANCE,
    // 세이브
    SAVE,
    // 경고
    YELLOW_CARD,
    // 퇴장
    RED_CARD,
    // 기타
    ETC
}