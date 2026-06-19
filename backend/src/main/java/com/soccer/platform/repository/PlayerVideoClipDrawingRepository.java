package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.PlayerVideoClipDrawingEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;

// 선수 개인 분석 클립 드로잉 Repository

public interface PlayerVideoClipDrawingRepository extends JpaRepository<PlayerVideoClipDrawingEntity, Integer> {

	// 특정 선수 개인 분석 클립에 연결된 드로잉 목록 조회
	// 정렬 기준
    // - 드로잉 표시 시작 시간 오름차순
    // - 같은 시작 시간에서는 ID 오름차순
    List<PlayerVideoClipDrawingEntity> findByPlayerVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(
            PlayerVideoClipEntity playerVideoClip
    );

    // 삭제되지 않은 선수 개인 분석 클립 드로잉 단건 조회
    Optional<PlayerVideoClipDrawingEntity> findByIdAndIsDeletedFalse(Integer drawingId);
}