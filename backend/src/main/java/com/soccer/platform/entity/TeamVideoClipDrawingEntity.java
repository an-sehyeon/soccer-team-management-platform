package com.soccer.platform.entity;

import com.soccer.platform.common.constants.DrawingTypeEnum;

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

/*
 * 팀 분석 클립 드로잉 엔티티
 * 팀 분석 클립 위에 표시할 선, 화살표, 원, 박스, 영역, 텍스트 데이터를 저장
 * 드로잉 시간은 생성된 팀 분석 클립 영상 기준 초로 저장
 */
@Entity
@Table(name = "team_video_clip_drawing")
@Getter
@Setter
@NoArgsConstructor
public class TeamVideoClipDrawingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_video_clip_id", nullable = false)
    private TeamVideoClipEntity teamVideoClip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(name = "drawing_type", nullable = false, length = 20)
    private DrawingTypeEnum drawingType;

    @Column(name = "start_time_sec", nullable = false)
    private Integer startTimeSec;

    @Column(name = "end_time_sec", nullable = false)
    private Integer endTimeSec;

    @Column(name = "drawing_data", nullable = false, columnDefinition = "JSON")
    private String drawingData;
}