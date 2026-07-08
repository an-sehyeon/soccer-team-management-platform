package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;

// 선수 기록 이벤트 Repository
public interface PlayerRecordEventRepository extends JpaRepository<PlayerRecordEventEntity, Integer> {

    // 삭제되지 않은 선수 기록 이벤트 단건 조회
    Optional<PlayerRecordEventEntity> findByIdAndIsDeletedFalse(Integer eventId);

    // 특정 선수 기록에 연결된 삭제되지 않은 이벤트 목록 조회
    List<PlayerRecordEventEntity> findByPlayerRecordAndIsDeletedFalseOrderByEventStartTimeSecAscCreatedAtAsc(
        PlayerRecordEntity playerRecord
    );
}