package com.soccer.platform.dto.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.ScheduleIntensityEnum;
import com.soccer.platform.common.constants.ScheduleTypeEnum;

/*
 * 스케줄 수정 요청 DTO
 *
 * PATCH 방식의 부분 수정을 기준으로 한다.
 * null로 들어온 값은 기존 값을 유지한다.
 */
@Getter
@Setter
public class UpdateScheduleRequestDTO {

    // 변경할 스케줄 날짜와 시간
    private LocalDateTime scheduleDatetime;

    // 변경할 장소
    private String place;

    // 변경할 스케줄 유형
    private ScheduleTypeEnum scheduleType;

    // 변경할 스케줄 상세 내용
    private String comment;

    // 변경할 훈련 강도
    private ScheduleIntensityEnum intensity;
}