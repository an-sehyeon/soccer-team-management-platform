package com.soccer.platform.service.teamclip;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;

// 팀 분석 클립 파일 생성 서비스
// FFmpeg를 실행해 원본 경기 영상에서 특정 구간을 mp4 파일로 생성

@Service
public class TeamAnalysisClipFileGenerator {

    private static final long FFMPEG_TIMEOUT_MINUTES = 30;

    @Value("${app.video.ffmpeg-path}")
    private String ffmpegPath;

    /*
     * 팀 분석 클립 파일 생성
     *
     * 1. 생성 명령 값 검증
     * 2. FFmpeg 명령어 생성
     * 3. FFmpeg 프로세스 실행
     * 4. 생성 파일 존재 여부 검증
     */
    public void generateTeamAnalysisClipFile(TeamAnalysisClipGenerationCommand command) {
        validateGenerationCommand(command);

        List<String> ffmpegCommand = createFfmpegCommand(command);

        runFfmpegCommand(ffmpegCommand);
        validateGeneratedFile(command);
    }

    // FFmpeg 명령어 생성
    private List<String> createFfmpegCommand(TeamAnalysisClipGenerationCommand command) {
        return List.of(
                ffmpegPath,
                "-y",
                "-ss",
                String.valueOf(command.getStartTimeSec()),
                "-i",
                command.getOriginalVideoFilePath().toString(),
                "-t",
                String.valueOf(command.getDurationSec()),
                "-map",
                "0:v:0",
                "-map",
                "0:a?",
                "-c:v",
                "libx264",
                "-preset",
                "veryfast",
                "-c:a",
                "aac",
                "-movflags",
                "+faststart",
                command.getTargetClipFilePath().toString()
        );
    }

    // FFmpeg 프로세스 실행
    private void runFfmpegCommand(List<String> ffmpegCommand) {
        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);

        try {
            Process process = processBuilder.start();

            boolean completed = process.waitFor(
                    FFMPEG_TIMEOUT_MINUTES,
                    TimeUnit.MINUTES
            );

            if (!completed) {
                process.destroyForcibly();
                throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
            }

            if (process.exitValue() != 0) {
                throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
            }
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        }
    }

    // 생성 명령 값 검증
    private void validateGenerationCommand(TeamAnalysisClipGenerationCommand command) {
        if (command == null
                || command.getTeamClipId() == null
                || command.getOriginalVideoFilePath() == null
                || command.getGeneratedClipFile() == null
                || command.getTargetClipFilePath() == null
                || command.getStartTimeSec() == null
                || command.getEndTimeSec() == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        }

        if (command.getStartTimeSec() < 0
                || command.getEndTimeSec() <= command.getStartTimeSec()) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (!Files.exists(command.getOriginalVideoFilePath())
                || !Files.isRegularFile(command.getOriginalVideoFilePath())) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND);
        }
    }

    // 생성된 파일 검증
    private void validateGeneratedFile(TeamAnalysisClipGenerationCommand command) {
        try {
            if (!Files.exists(command.getTargetClipFilePath())
                    || !Files.isRegularFile(command.getTargetClipFilePath())
                    || Files.size(command.getTargetClipFilePath()) <= 0) {
                throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
            }
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED);
        }
    }
}