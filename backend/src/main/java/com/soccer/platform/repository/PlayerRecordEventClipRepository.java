package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soccer.platform.common.constants.PlayerRecordClipSourceTypeEnum;
import com.soccer.platform.common.constants.PlayerRecordEventTypeEnum;
import com.soccer.platform.entity.PlayerRecordEventClipEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;

// 선수 기록 이벤트 클립 연결 Repository
public interface PlayerRecordEventClipRepository
        extends JpaRepository<PlayerRecordEventClipEntity, Integer> {

    // 삭제되지 않은 선수 기록 이벤트 클립 연결 단건 조회
    Optional<PlayerRecordEventClipEntity> findByIdAndIsDeletedFalse(
            Integer eventClipId
    );

    // 특정 선수 기록 이벤트에 연결된 삭제되지 않은 클립 목록 조회
    List<PlayerRecordEventClipEntity>
            findByPlayerRecordEventAndIsDeletedFalse(
                    PlayerRecordEventEntity playerRecordEvent
            );

    // 여러 선수 기록 이벤트에 연결된 삭제되지 않은 클립 목록 조회
    List<PlayerRecordEventClipEntity>
            findByPlayerRecordEventInAndIsDeletedFalse(
                    List<PlayerRecordEventEntity> playerRecordEvents
            );

    /*
     * 같은 팀 분석 클립과 같은 이벤트 유형의
     * 활성 연결이 존재하는지 조회한다.
     *
     * 연결과 이벤트가 모두 소프트 삭제되지 않은 경우만
     * 중복으로 판단한다.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(eventClip.id) > 0
                       THEN true
                       ELSE false
                   END
            FROM PlayerRecordEventClipEntity eventClip
            JOIN eventClip.playerRecordEvent event
            WHERE eventClip.isDeleted = false
              AND event.isDeleted = false
              AND eventClip.clipSourceType = :clipSourceType
              AND eventClip.teamVideoClip.id = :teamClipId
              AND event.eventType = :eventType
            """)
    boolean existsActiveTeamClipEventType(
            @Param("clipSourceType")
            PlayerRecordClipSourceTypeEnum clipSourceType,

            @Param("teamClipId")
            Integer teamClipId,

            @Param("eventType")
            PlayerRecordEventTypeEnum eventType
    );

    /*
     * 같은 선수 개인 분석 클립과 같은 이벤트 유형의
     * 활성 연결이 존재하는지 조회한다.
     *
     * 연결과 이벤트가 모두 소프트 삭제되지 않은 경우만
     * 중복으로 판단한다.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(eventClip.id) > 0
                       THEN true
                       ELSE false
                   END
            FROM PlayerRecordEventClipEntity eventClip
            JOIN eventClip.playerRecordEvent event
            WHERE eventClip.isDeleted = false
              AND event.isDeleted = false
              AND eventClip.clipSourceType = :clipSourceType
              AND eventClip.playerVideoClip.id = :playerClipId
              AND event.eventType = :eventType
            """)
    boolean existsActivePlayerClipEventType(
            @Param("clipSourceType")
            PlayerRecordClipSourceTypeEnum clipSourceType,

            @Param("playerClipId")
            Integer playerClipId,

            @Param("eventType")
            PlayerRecordEventTypeEnum eventType
    );
}