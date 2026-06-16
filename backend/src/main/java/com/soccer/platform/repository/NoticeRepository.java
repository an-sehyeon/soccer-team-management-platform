package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.NoticeEntity;

/**
 * 공지사항 Repository
 *
 * 삭제되지 않은 공지사항만 조회한다.
 */
public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {

    // 삭제되지 않은 공지사항 단건 조회
    Optional<NoticeEntity> findByIdAndIsDeletedFalse(Integer noticeId);

    // 삭제되지 않은 공지사항 전체 목록 조회
    // 정렬 조건은 Service에서 Pageable로 전달한다.
    Page<NoticeEntity> findByIsDeletedFalse(Pageable pageable);

    // 삭제되지 않은 중요 공지사항 목록 조회
    // importantOnly=true 요청일 때 사용한다.
    Page<NoticeEntity> findByIsDeletedFalseAndIsImportantTrue(Pageable pageable);
}