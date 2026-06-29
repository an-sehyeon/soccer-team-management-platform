package com.soccer.platform.service.metadata;

import java.nio.file.Path;

// 경기 영상 메타데이터 추출 인터페이스
// 현재는 영상 길이 추출에 사용, 추후 해상도/썸네일/비트레이트 추출로 확장 가능
public interface MatchVideoMetadataExtractor {

	// 경기 영상 길이 초 단위 추출
	Integer extractDurationSec(Path videoFilePath); 
}
