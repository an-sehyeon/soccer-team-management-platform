package com.soccer.platform.service.teamclip;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 팀 분석 클립 파일 비동기 생성 서비스
// API 응답과 분리해서 FFmpeg 클립 생성 작업을 실행

@Service
@RequiredArgsConstructor
public class TeamAnalysisClipGenerationAsyncService {

    private final TeamAnalysisClipFileGenerator teamAnalysisClipFileGenerator;
    private final TeamAnalysisClipGenerationStatusService teamAnalysisClipGenerationStatusService;
    private final TeamAnalysisClipLocalFileService teamAnalysisClipLocalFileService;

    /*
     * 팀 분석 클립 파일 비동기 생성
     *
     * 1. FFmpeg로 원본 경기 영상 구간을 mp4 파일로 생성
     * 2. 성공 시 team_video_clip.url 저장 및 READY 변경
     * 3. 기존 생성 파일이 있으면 삭제
     * 4. 실패 시 team_video_clip.status를 FAILED로 변경
     */
    @Async
    public void generateTeamAnalysisClipFileAsync(TeamAnalysisClipGenerationCommand command) {
        try {
            teamAnalysisClipFileGenerator.generateTeamAnalysisClipFile(command);

            teamAnalysisClipGenerationStatusService.markGenerationReady(
                    command.getTeamClipId(),
                    command.getGeneratedClipUrl()
            );

            if (command.hasPreviousClipUrl()) {
                teamAnalysisClipLocalFileService.deleteGeneratedClipFileIfExists(
                        command.getPreviousClipUrl()
                );
            }
        } catch (Exception exception) {
            teamAnalysisClipGenerationStatusService.markGenerationFailed(
                    command.getTeamClipId()
            );
        }
    }
}