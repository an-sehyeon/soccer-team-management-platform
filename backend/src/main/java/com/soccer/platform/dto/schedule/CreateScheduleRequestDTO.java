package com.soccer.platform.dto.schedule;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.ScheduleIntensityEnum;
import com.soccer.platform.common.constants.ScheduleTypeEnum;

import lombok.Getter;
import lombok.Setter;

/*
 * 스케줄 등록 요청 DTO
 *
 * 지도자가 새로운 팀 일정을 등록할 때 사용한다.
 * memberId는 요청에서 받지 않고, JWT 인증 정보의 로그인 회원 ID를 사용한다.
 */
@Getter
@Setter
public class CreateScheduleRequestDTO {

    // 스케줄 날짜와 시간
    private LocalDateTime scheduleDateTime;

    // 장소
    private String place;

    // 스케줄 유형
    // TRAINING, MATCH, MEETING, EVENT, EXTERNAL, ETC
    private ScheduleTypeEnum scheduleType;

    // 스케줄 상세 내용
    private String comment;

    /*
     * 훈련 강도
     * HIGH, MEDIUM, LOW
     * 경기, 회의, 행사 일정에서는 null일 수 있다.
     */
    private ScheduleIntensityEnum intensity;
}