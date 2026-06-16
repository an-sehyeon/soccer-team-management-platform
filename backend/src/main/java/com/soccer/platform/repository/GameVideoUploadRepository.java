package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.GameVideoUploadEntity;

// 경기 영상 업로드 Repository
public interface GameVideoUploadRepository extends JpaRepository<GameVideoUploadEntity, Integer> {

    Page<GameVideoUploadEntity> findByIsDeletedFalse(Pageable pageable);

    Optional<GameVideoUploadEntity> findByIdAndIsDeletedFalse(Integer matchVideoId);
}