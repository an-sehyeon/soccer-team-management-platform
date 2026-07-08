package com.soccer.platform.entity;

import com.soccer.platform.common.constants.PlayerRecordClipSourceTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 선수 기록 이벤트 클립 연결 Entity
// 선수 기록 이벤트와 팀 분석 클립 또는 선수 개인 분석 클립의 선택 연결 정보를 저장
@Entity
@Table(
    name = "player_record_event_clip",
    indexes = {
        @Index(
            name = "idx_player_record_event_clip_event_deleted",
            columnList = "player_record_event_id, is_deleted"
        ),
        @Index(
            name = "idx_player_record_event_clip_team_deleted",
            columnList = "team_clip_id, is_deleted"
        ),
        @Index(
            name = "idx_player_record_event_clip_player_deleted",
            columnList = "player_clip_id, is_deleted"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerRecordEventClipEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // 연결 대상 선수 기록 이벤트
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_record_event_id", nullable = false)
    private PlayerRecordEventEntity playerRecordEvent;

    // 연결 클립 출처 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "clip_source_type", nullable = false, length = 30)
    private PlayerRecordClipSourceTypeEnum clipSourceType;

    // 팀 분석 클립
    // clip_source_type = TEAM_ANALYSIS인 경우 사용한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_clip_id")
    private TeamVideoClipEntity teamVideoClip;

    // 선수 개인 분석 클립
    // clip_source_type = PLAYER_ANALYSIS인 경우 사용한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_clip_id")
    private PlayerVideoClipEntity playerVideoClip;

    // 팀 분석 클립 연결 생성
    public static PlayerRecordEventClipEntity createTeamClipLink(
        PlayerRecordEventEntity playerRecordEvent,
        TeamVideoClipEntity teamVideoClip
    ) {
        PlayerRecordEventClipEntity eventClip = new PlayerRecordEventClipEntity();

        eventClip.playerRecordEvent = playerRecordEvent;
        eventClip.clipSourceType = PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS;
        eventClip.teamVideoClip = teamVideoClip;
        eventClip.playerVideoClip = null;
        eventClip.setIsDeleted(false);

        return eventClip;
    }

    // 선수 개인 분석 클립 연결 생성
    public static PlayerRecordEventClipEntity createPlayerClipLink(
        PlayerRecordEventEntity playerRecordEvent,
        PlayerVideoClipEntity playerVideoClip
    ) {
        PlayerRecordEventClipEntity eventClip = new PlayerRecordEventClipEntity();

        eventClip.playerRecordEvent = playerRecordEvent;
        eventClip.clipSourceType = PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS;
        eventClip.teamVideoClip = null;
        eventClip.playerVideoClip = playerVideoClip;
        eventClip.setIsDeleted(false);

        return eventClip;
    }

    // 선수 기록 이벤트 클립 연결 소프트 삭제
    public void softDelete() {
        this.setIsDeleted(true);
    }
}