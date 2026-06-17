package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.TeamVideoClipDrawingEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;

// 팀 분석 클립 드로잉 Repository
public interface TeamVideoClipDrawingRepository extends JpaRepository<TeamVideoClipDrawingEntity, Integer> {

	Optional<TeamVideoClipDrawingEntity> findByIdAndIsDeletedFalse(Integer drawingId);
	
	List<TeamVideoClipDrawingEntity> findByTeamVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(
	            TeamVideoClipEntity teamVideoClip);
}
