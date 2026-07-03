package com.soccer.platform.service.playerclip;

import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 파일 생성 명령 객체
// 비동기 FFmpeg 작업에 필요한 값을 하나로 묶어 전달

@Getter
@AllArgsConstructor
public class PlayerAnalysisClipGenerationCommand {

	private final Integer playerClipId;
	private final Path originalVideoFilePath;
	private final GeneratedPlayerAnalysisClipFile generatedClipFile;
	private final Integer startTimeSec;
	private final Integer endTimeSec;
	private final String previousClipUrl;

	// 생성할 클립 길이 계산
	public Integer getDurationSec() {
		return endTimeSec - startTimeSec;
	}

	// 생성될 클립 파일 저장 경로 반환
	public Path getTargetClipFilePath() {
		return generatedClipFile.getStoredFilePath();
	}

	// 생성된 클립 파일 접근 URL 반환
	public String getGeneratedClipUrl() {
		return generatedClipFile.getAccessUrl();
	}
	
	// 이전 생성 파일 존재 여부 반환
	public boolean hasPreviousClipUrl() {
		return previousClipUrl != null && !previousClipUrl.isBlank();
	}

}