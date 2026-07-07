package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.TeamVideoClipEntity;

// 팀 분석 클립 Repository

public interface TeamVideoClipRepository extends JpaRepository<TeamVideoClipEntity, Integer> {

    Page<TeamVideoClipEntity> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            VideoUploadStatusEnum status,
            Pageable pageable
    );

    Page<TeamVideoClipEntity> findByGameVideoUpload_IdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer matchVideoId,
            VideoUploadStatusEnum status,
            Pageable pageable
    );

    Page<TeamVideoClipEntity> findByClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            TeamVideoClipTypeEnum clipType,
            VideoUploadStatusEnum status,
            Pageable pageable
    );

    Page<TeamVideoClipEntity> findByGameVideoUpload_IdAndClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer matchVideoId,
            TeamVideoClipTypeEnum clipType,
            VideoUploadStatusEnum status,
            Pageable pageable
    );

    Optional<TeamVideoClipEntity> findByIdAndIsDeletedFalse(Integer id);
}