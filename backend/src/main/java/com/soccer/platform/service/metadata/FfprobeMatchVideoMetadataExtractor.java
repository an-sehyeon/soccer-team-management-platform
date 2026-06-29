package com.soccer.platform.service.metadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;

// ffprobe 기반 경기 영상 메타데이터 추출 서비스
// 로컬에 설치된 ffprobe를 실행해 영상 길이를 초 단위로 추출
@Service
public class FfprobeMatchVideoMetadataExtractor implements MatchVideoMetadataExtractor {

	private static final long FFPROBE_TIMEOUT_SECONDS = 10L;
	
	@Value("${app.video.ffprobe-path:ffprobe}")
	private String ffprobePath;
	
	// 경기 영상 길이 초 단위 추출
	@Override
	public Integer extractDurationSec(Path videoFilePath) {
		ProcessBuilder processBuilder = new ProcessBuilder(
				ffprobePath,
				"-v", "error",
				"-show_entries", "format=duration",
				"-of", "default=noprint_wrappers=1:nokey=1",
				videoFilePath.toString()
		);
		
		try {
			Process process = processBuilder.start();

			boolean finished = process.waitFor(FFPROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			if (!finished) {
				process.destroyForcibly();
				throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_EXTRACTION_FAILED);
			}

			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

			if (process.exitValue() != 0) {
				throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_EXTRACTION_FAILED);
			}

			return parseDurationSec(output);

		} catch (IOException e) {
			throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_EXTRACTION_FAILED);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_EXTRACTION_FAILED);
		}
	}

	// ffprobe 출력값을 초 단위 정수로 변환
	private Integer parseDurationSec(String output) {
		try {
			double duration = Double.parseDouble(output);

			if (Double.isNaN(duration) || Double.isInfinite(duration) || duration <= 0) {
				throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_DURATION);
			}

			if (duration > Integer.MAX_VALUE) {
				throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_DURATION);
			}

			return (int) Math.ceil(duration);

		} catch (NumberFormatException e) {
			throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_EXTRACTION_FAILED);
		}
	}
	
}