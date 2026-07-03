package com.soccer.platform.service.playerclip;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.PlayerVideoClipRepository;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 파일 생성 상태 변경 서비스
// FFmpeg 작업 성공/실패 결과를 player_video_clip 상태에 반영

@Service
@RequiredArgsConstructor
public class PlayerAnalysisClipGenerationStatusService {

	private final PlayerVideoClipRepository playerVideoClipRepository;

	// 선수 개인 분석 클립 파일 생성 성공 처리
	@Transactional
	public void markGenerationReady(Integer playerClipId, String generatedClipUrl) {
		PlayerVideoClipEntity playerVideoClip = findActivePlayerClip(playerClipId);

		playerVideoClip.markGenerationReady(generatedClipUrl);
	}

	// 선수 개인 분석 클립 파일 생성 실패 처리
	@Transactional
	public void markGenerationFailed(Integer playerClipId) {
		PlayerVideoClipEntity playerVideoClip = findActivePlayerClip(playerClipId);

		playerVideoClip.markGenerationFailed();
	}

	// 삭제되지 않은 선수 개인 분석 클립 조회
	private PlayerVideoClipEntity findActivePlayerClip(Integer playerClipId) {
		if (playerClipId == null) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
		}

		return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
				.orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
	}

}