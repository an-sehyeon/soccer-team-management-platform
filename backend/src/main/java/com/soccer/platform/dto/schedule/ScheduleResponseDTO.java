package com.soccer.platform.dto.schedule;

import com.soccer.platform.common.constants.ScheduleIntensityEnum;
import com.soccer.platform.common.constants.ScheduleTypeEnum;
import com.soccer.platform.entity.ScheduleEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 스케줄 응답 DTO
 *
 * 지도자, 선수, 분석관이 스케줄을 조회할 때 공통으로 사용한다.
 */
@Getter
@Setter
@Builder
public class ScheduleResponseDTO {

    private Integer scheduleId;
    private Integer writerMemberId;
    private String writerName;
    private LocalDateTime scheduleDatetime;
    private String place;
    private ScheduleTypeEnum scheduleType;
    private String comment;
    private ScheduleIntensityEnum intensity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ScheduleEntity를 응답 DTO로 변환한다.
    public static ScheduleResponseDTO from(ScheduleEntity schedule) {
        return ScheduleResponseDTO.builder()
                .scheduleId(schedule.getId())
                .writerMemberId(schedule.getMember().getId())
                .writerName(schedule.getMember().getName())
                .scheduleDatetime(schedule.getScheduleDatetime())
                .place(schedule.getPlace())
                .scheduleType(schedule.getScheduleType())
                .comment(schedule.getComment())
                .intensity(schedule.getIntensity())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}