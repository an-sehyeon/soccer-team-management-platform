package com.soccer.platform.service.teamclip;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipRepository;

import lombok.RequiredArgsConstructor;

// 팀 분석 클립 파일 생성 상태 변경 서비스
// FFmpeg 작업 성공/실패 결과를 team_video_clip 상태에 반영

@Service
@RequiredArgsConstructor
public class TeamAnalysisClipGenerationStatusService {

    private final TeamVideoClipRepository teamVideoClipRepository;

    // 팀 분석 클립 파일 생성 성공 처리
    @Transactional
    public void markGenerationReady(Integer teamClipId, String generatedClipUrl) {
        TeamVideoClipEntity teamVideoClip = findActiveTeamClip(teamClipId);

        teamVideoClip.markGenerationReady(generatedClipUrl);
    }

    // 팀 분석 클립 파일 생성 실패 처리
    @Transactional
    public void markGenerationFailed(Integer teamClipId) {
        TeamVideoClipEntity teamVideoClip = findActiveTeamClip(teamClipId);

        teamVideoClip.markGenerationFailed();
    }

    // 삭제되지 않은 팀 분석 클립 조회
    private TeamVideoClipEntity findActiveTeamClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }
}