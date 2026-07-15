package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.TeamVideoClipEntity;

import jakarta.persistence.LockModeType;

// 팀 분석 클립 Repository
public interface TeamVideoClipRepository
        extends JpaRepository<TeamVideoClipEntity, Integer> {

    Page<TeamVideoClipEntity>
            findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                    VideoUploadStatusEnum status,
                    Pageable pageable
            );

    Page<TeamVideoClipEntity>
            findByGameVideoUpload_IdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                    Integer matchVideoId,
                    VideoUploadStatusEnum status,
                    Pageable pageable
            );

    Page<TeamVideoClipEntity>
            findByClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                    TeamVideoClipTypeEnum clipType,
                    VideoUploadStatusEnum status,
                    Pageable pageable
            );

    Page<TeamVideoClipEntity>
            findByGameVideoUpload_IdAndClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                    Integer matchVideoId,
                    TeamVideoClipTypeEnum clipType,
                    VideoUploadStatusEnum status,
                    Pageable pageable
            );

    Optional<TeamVideoClipEntity> findByIdAndIsDeletedFalse(
            Integer id
    );

    /*
     * 선수 기록 이벤트 연결에 사용할 팀 분석 클립을
     * 비관적 쓰기 잠금으로 조회한다.
     *
     * 동일 클립에 대한 동시 등록 요청을 직렬화해
     * 같은 이벤트 유형의 중복 연결을 방지한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT teamClip
            FROM TeamVideoClipEntity teamClip
            WHERE teamClip.id = :teamClipId
              AND teamClip.isDeleted = false
            """)
    Optional<TeamVideoClipEntity>
            findByIdAndIsDeletedFalseForUpdate(
                    @Param("teamClipId") Integer teamClipId
            );
}