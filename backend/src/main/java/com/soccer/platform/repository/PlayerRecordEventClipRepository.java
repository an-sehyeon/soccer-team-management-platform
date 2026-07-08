package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.PlayerRecordEventClipEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;

// 선수 기록 이벤트 클립 연결 Repository
public interface PlayerRecordEventClipRepository extends JpaRepository<PlayerRecordEventClipEntity, Integer> {

    // 삭제되지 않은 선수 기록 이벤트 클립 연결 단건 조회
    Optional<PlayerRecordEventClipEntity> findByIdAndIsDeletedFalse(Integer eventClipId);

    // 특정 선수 기록 이벤트에 연결된 삭제되지 않은 클립 목록 조회
    List<PlayerRecordEventClipEntity> findByPlayerRecordEventAndIsDeletedFalse(
        PlayerRecordEventEntity playerRecordEvent
    );

    // 특정 선수 기록 이벤트에 연결된 삭제되지 않은 클립 목록 조회
    List<PlayerRecordEventClipEntity> findByPlayerRecordEventInAndIsDeletedFalse(
        List<PlayerRecordEventEntity> playerRecordEvents
    );
}