package com.soccer.platform.entity;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.ScheduleIntensityEnum;
import com.soccer.platform.common.constants.ScheduleTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스케줄 엔티티
 *
 * 지도자가 등록한 훈련, 경기, 미팅 등의 일정을 관리한다.
 * 선수는 등록된 스케줄을 조회만 할 수 있다.
 */
@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
public class ScheduleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "schedule_datetime", nullable = false)
    private LocalDateTime scheduleDatetime;

    @Column(name = "place", nullable = false, length = 30)
    private String place;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleTypeEnum scheduleType;

    @Column(name = "comment", length = 255)
    private String comment;

    @Column(name = "intensity", length = 10)
    private ScheduleIntensityEnum intensity;
}
