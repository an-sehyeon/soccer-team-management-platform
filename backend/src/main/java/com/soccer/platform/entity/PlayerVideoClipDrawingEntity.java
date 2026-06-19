package com.soccer.platform.entity;

import com.soccer.platform.common.constants.DrawingTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 선수 개인 분석 클립 드로잉 엔티티
 *
 * 선수 개인 클립 위에 표시할 선, 화살표, 원, 박스, 영역, 텍스트 데이터를 저장한다.
 * 개인 클립 드로잉은 대상 선수 본인과 지도자만 조회할 수 있어야 한다.
 */
@Entity
@Table(name = "player_video_clip_drawing")
@Getter
@Setter
@NoArgsConstructor
public class PlayerVideoClipDrawingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_video_clip_id", nullable = false)
    private PlayerVideoClipEntity playerVideoClip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "drawing_type", nullable = false, length = 20)
    private DrawingTypeEnum drawingType;

    @Column(name = "start_time_sec", nullable = false)
    private Integer startTimeSec;

    @Column(name = "end_time_sec", nullable = false)
    private Integer endTimeSec;

    @Column(name = "drawing_data", nullable = false, columnDefinition = "JSON")
    private String drawingData;
}
