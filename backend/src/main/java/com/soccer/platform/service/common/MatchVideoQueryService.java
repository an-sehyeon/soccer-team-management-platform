package com.soccer.platform.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;

import lombok.RequiredArgsConstructor;

// 경기 영상 조회 전용 Service

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchVideoQueryService {

    private final GameVideoUploadRepository gameVideoUploadRepository;

    // 삭제되지 않은 경기 영상을 조회
    // 경기 영상, 팀 클립, 선수 개인 클립, 선수 기록에서 공통으로 사용
    public GameVideoUploadEntity findActiveMatchVideoById(
            Integer matchVideoId,
            ErrorCode notFoundErrorCode
    ) {
        if (matchVideoId == null) {
            throw new CustomException(notFoundErrorCode);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(matchVideoId)
                .orElseThrow(() -> new CustomException(notFoundErrorCode));
    }

    // 영상 길이 정보가 준비된 경기 영상을 조회
    // 선수 개인 분석 클립처럼 원본 영상 길이 기준 검증이 필요한 기능에서 사용
    public GameVideoUploadEntity findActiveMatchVideoWithDuration(
            Integer matchVideoId,
            ErrorCode notFoundErrorCode,
            ErrorCode durationNotReadyErrorCode
    ) {
        GameVideoUploadEntity matchVideo = findActiveMatchVideoById(
                matchVideoId,
                notFoundErrorCode
        );

        validateDurationReady(matchVideo, durationNotReadyErrorCode);

        return matchVideo;
    }

    // 원본 영상 길이 정보 준비 여부 검증
    // durationSec가 null이거나 0 이하이면 아직 클립 시간 검증에 사용할 수 없다.
    public void validateDurationReady(
            GameVideoUploadEntity matchVideo,
            ErrorCode durationNotReadyErrorCode
    ) {
        if (matchVideo == null
                || matchVideo.getDurationSec() == null
                || matchVideo.getDurationSec() <= 0) {
            throw new CustomException(durationNotReadyErrorCode);
        }
    }
}