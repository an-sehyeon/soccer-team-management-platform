package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;

// 선수 개인 분석 클립 Repository

public interface PlayerVideoClipRepository extends JpaRepository<PlayerVideoClipEntity, Integer>{

	// 삭제되지 않은 개인 분석 클립 ID 조회
    Optional<PlayerVideoClipEntity> findByIdAndIsDeletedFalse(Integer id);

    // 삭제되지 않은 개인 분석 클립 전체 목록 조회
    Page<PlayerVideoClipEntity> findByIsDeletedFalse(Pageable pageable);

    // 특정 원본 경기 영상에 연결된 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByGameVideoUploadAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            Pageable pageable
    );

    // 특정 선수에게 지정된 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByPlayerAndIsDeletedFalse(
            MemberEntity player,
            Pageable pageable
    );

    // 특정 선수와 특정 원본 경기 영상에 해당하는 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByPlayerAndGameVideoUploadAndIsDeletedFalse(
            MemberEntity player,
            GameVideoUploadEntity gameVideoUpload,
            Pageable pageable
    );

    // 특정 선수와 특정 클립 유형에 해당하는 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByPlayerAndClipTypeAndIsDeletedFalse(
            MemberEntity player,
            PlayerClipTypeEnum clipType,
            Pageable pageable
    );

    // 특정 원본 경기 영상과 특정 클립 유형에 해당하는 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByGameVideoUploadAndClipTypeAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            PlayerClipTypeEnum clipType,
            Pageable pageable
    );

    // 특정 선수, 특정 원본 경기 영상, 특정 클립 유형에 해당하는 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByPlayerAndGameVideoUploadAndClipTypeAndIsDeletedFalse(
            MemberEntity player,
            GameVideoUploadEntity gameVideoUpload,
            PlayerClipTypeEnum clipType,
            Pageable pageable
    );
    
    // 특정 클립 유형에 해당하는 삭제되지 않은 개인 분석 클립 목록 조회
    Page<PlayerVideoClipEntity> findByClipTypeAndIsDeletedFalse(
            PlayerClipTypeEnum clipType,
            Pageable pageable
    );
}
