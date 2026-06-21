package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;

// 선수 기록 Repository

public interface PlayerRecordRepository extends JpaRepository<PlayerRecordEntity, Integer> {

	// 삭제되지 않은 선수 기록 단건 조회
    Optional<PlayerRecordEntity> findByIdAndIsDeletedFalse(Integer recordId);

    // 같은 경기 영상과 같은 선수의 활성 기록이 이미 존재하는지 확인
    // 선수 기록은 한 경기당 한 선수 기록 1개만 허용
    boolean existsByGameVideoUploadAndPlayerAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player
    );

    // 수정 시 자기 자신을 제외하고 같은 경기 영상과 같은 선수의 활성 기록이 존재하는지 확인
    // - 기존 기록 ID 1번을 수정할 때 ID 1번은 중복 검사에서 제외
    //  다른 recordId에 같은 uploadId + playerId 기록이 있으면 중복으로 처리
    boolean existsByGameVideoUploadAndPlayerAndIsDeletedFalseAndIdNot(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            Integer recordId
    );
    
    // 선수 기록 수정 시 중복 기록이 있는지 확인
    // 같은 경기 영상 + 같은 선수 + 삭제되지 않은 기록 + 현재 수정 중인 기록은 제외
    boolean existsByGameVideoUploadAndPlayerAndIdNotAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            Integer id
    );

    // 관리용 선수 기록 전체 목록 조회
    Page<PlayerRecordEntity> findByIsDeletedFalse(Pageable pageable);

    // 특정 경기 영상 기준 선수 기록 목록 조회
    Page<PlayerRecordEntity> findByGameVideoUploadAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            Pageable pageable
    );

    // 특정 선수 기준 선수 기록 목록 조회
    Page<PlayerRecordEntity> findByPlayerAndIsDeletedFalse(
            MemberEntity player,
            Pageable pageable
    );

    // 특정 경기 영상 + 특정 선수 기준 선수 기록 목록 조회
    // 관리 화면에서 경기와 선수를 함께 필터링할 때 사용
    Page<PlayerRecordEntity> findByGameVideoUploadAndPlayerAndIsDeletedFalse(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            Pageable pageable
    );
    
}