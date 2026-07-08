package com.soccer.platform.entity;

import com.soccer.platform.common.constants.PlayerRecordEventTypeEnum;

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

// 선수 기록 이벤트 Entity
// 경기별 선수 요약 기록(player_record)에 연결되는 개별 장면/이벤트 기록을 저장
@Entity
@Table(
    name = "player_record_event",
    indexes = {
        @Index(
            name = "idx_player_record_event_record_deleted",
            columnList = "player_record_id, is_deleted"
        ),
        @Index(
            name = "idx_player_record_event_type_deleted",
            columnList = "event_type, is_deleted"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerRecordEventEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // 선수 경기 요약 기록
    // upload_id, player_id는 직접 저장하지 않고 player_record_id를 통해 확인한다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_record_id", nullable = false)
    private PlayerRecordEntity playerRecord;

    // 이벤트 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private PlayerRecordEventTypeEnum eventType;

    // 원본 경기 영상 기준 이벤트 구간 시작 시간(초)
    @Column(name = "event_start_time_sec", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer eventStartTimeSec;

    // 원본 경기 영상 기준 이벤트 구간 종료 시간(초)
    @Column(name = "event_end_time_sec", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer eventEndTimeSec;

    // 이벤트 수치
    // 대부분 1을 사용하지만, 한 번에 여러 개를 반영해야 하는 경우 확장을 위해 유지한다.
    @Column(name = "value", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 1")
    private Integer value;

    // 이벤트 단위 메모
    // player_record.memo와 성격이 다르며 DTO에서는 eventMemo로 표현한다.
    @Column(name = "memo", length = 255)
    private String memo;

    // 이벤트 등록자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private MemberEntity createdBy;

    // 선수 기록 이벤트 생성
    public static PlayerRecordEventEntity create(
        PlayerRecordEntity playerRecord,
        PlayerRecordEventTypeEnum eventType,
        Integer eventStartTimeSec,
        Integer eventEndTimeSec,
        Integer value,
        String memo,
        MemberEntity createdBy
    ) {
        PlayerRecordEventEntity event = new PlayerRecordEventEntity();

        event.playerRecord = playerRecord;
        event.eventType = eventType;
        event.eventStartTimeSec = eventStartTimeSec;
        event.eventEndTimeSec = eventEndTimeSec;
        event.value = value;
        event.memo = memo;
        event.createdBy = createdBy;
        event.setIsDeleted(false);

        return event;
    }

    // 선수 기록 이벤트 수정
    public void update(
        PlayerRecordEventTypeEnum eventType,
        Integer eventStartTimeSec,
        Integer eventEndTimeSec,
        Integer value,
        String memo
    ) {
        this.eventType = eventType;
        this.eventStartTimeSec = eventStartTimeSec;
        this.eventEndTimeSec = eventEndTimeSec;
        this.value = value;
        this.memo = memo;
    }

    // 선수 기록 이벤트 소프트 삭제
    public void softDelete() {
        this.setIsDeleted(true);
    }
}