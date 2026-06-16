package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.TeamVideoClipEntity;

// 팀 분석 클립 Repository
public interface TeamVideoClipRepository extends JpaRepository<TeamVideoClipEntity, Integer>{

	Page<TeamVideoClipEntity> findByIsDeletedFalse(Pageable pageable);
    
	Page<TeamVideoClipEntity> findByGameVideoUpload_IdAndIsDeletedFalse(
            Integer matchVideoId,
            Pageable pageable
    );
    
	Optional<TeamVideoClipEntity> findByIdAndIsDeletedFalse(Integer id);

}
