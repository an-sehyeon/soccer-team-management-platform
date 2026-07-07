package com.soccer.platform.service.teamclip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.GameVideoUploadEntity;

import jakarta.annotation.PostConstruct;

// 팀 분석 클립 로컬 파일 경로 관리 서비스
// 원본 경기 영상 파일 경로 계산과 생성될 클립 파일 경로 생성을 담당

@Service
public class TeamAnalysisClipLocalFileService {

    @Value("${app.upload.match-video-dir}")
    private String matchVideoUploadDir;

    @Value("${app.upload.match-video-url-prefix}")
    private String matchVideoUrlPrefix;

    @Value("${app.upload.team-analysis-clip-dir}")
    private String teamAnalysisClipUploadDir;

    @Value("${app.upload.team-analysis-clip-url-prefix}")
    private String teamAnalysisClipUrlPrefix;

    private Path matchVideoUploadPath;
    private Path teamAnalysisClipUploadPath;

    // 팀 분석 클립 저장 디렉터리 초기화
    @PostConstruct
    public void initializeTeamAnalysisClipDirectory() {
        try {
            this.matchVideoUploadPath = Paths.get(matchVideoUploadDir)
                    .toAbsolutePath()
                    .normalize();

            this.teamAnalysisClipUploadPath = Paths.get(teamAnalysisClipUploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(teamAnalysisClipUploadPath);
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DIRECTORY_CREATE_FAILED);
        }
    }

    // 원본 경기 영상 URL에서 실제 로컬 파일 경로 계산
    public Path resolveOriginalMatchVideoFilePath(GameVideoUploadEntity matchVideo) {
        if (matchVideo == null || matchVideo.getUrl() == null || matchVideo.getUrl().isBlank()) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND);
        }

        String storedFileName = extractStoredFileName(
                matchVideo.getUrl(),
                matchVideoUrlPrefix
        );

        Path originalVideoFilePath = matchVideoUploadPath
                .resolve(storedFileName)
                .normalize();

        if (!originalVideoFilePath.startsWith(matchVideoUploadPath)
                || !Files.exists(originalVideoFilePath)
                || !Files.isRegularFile(originalVideoFilePath)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND);
        }

        return originalVideoFilePath;
    }

    // 생성될 팀 분석 클립 파일 정보 생성
    public GeneratedTeamAnalysisClipFile createGeneratedClipFile(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        String storedFileName = createStoredClipFileName(teamClipId);

        Path targetClipFilePath = teamAnalysisClipUploadPath
                .resolve(storedFileName)
                .normalize();

        if (!targetClipFilePath.startsWith(teamAnalysisClipUploadPath)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        }

        String accessUrl = teamAnalysisClipUrlPrefix + "/" + storedFileName;

        return new GeneratedTeamAnalysisClipFile(
                storedFileName,
                accessUrl,
                targetClipFilePath
        );
    }

    // 기존 팀 분석 클립 파일 삭제
    public void deleteGeneratedClipFileIfExists(String clipUrl) {
        if (clipUrl == null || clipUrl.isBlank()) {
            return;
        }

        try {
            String storedFileName = extractStoredFileName(
                    clipUrl,
                    teamAnalysisClipUrlPrefix
            );

            Path targetClipFilePath = teamAnalysisClipUploadPath
                    .resolve(storedFileName)
                    .normalize();

            if (!targetClipFilePath.startsWith(teamAnalysisClipUploadPath)) {
                return;
            }

            Files.deleteIfExists(targetClipFilePath);
        } catch (IOException exception) {
            // 파일 삭제 실패는 클립 생성 성공 상태를 되돌리지 않는다.
        }
    }

    // 접근 URL에서 저장 파일명 추출
    private String extractStoredFileName(String accessUrl, String urlPrefix) {
        String normalizedUrlPrefix = removeTrailingSlash(urlPrefix);
        String requiredPrefix = normalizedUrlPrefix + "/";

        if (!accessUrl.startsWith(requiredPrefix)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND);
        }

        String storedFileName = accessUrl.substring(requiredPrefix.length());

        if (storedFileName.isBlank()
                || storedFileName.contains("/")
                || storedFileName.contains("\\")) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND);
        }

        return storedFileName;
    }

    // URL prefix 마지막 슬래시 제거
    private String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        }

        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    // 팀 분석 클립 저장 파일명 생성
    private String createStoredClipFileName(Integer teamClipId) {
        return "team-clip-" + teamClipId + "-" + UUID.randomUUID() + ".mp4";
    }
}