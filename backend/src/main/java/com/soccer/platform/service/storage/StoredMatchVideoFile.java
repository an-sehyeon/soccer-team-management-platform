package com.soccer.platform.service.storage;

import lombok.AllArgsConstructor;
import java.nio.file.Path;
import lombok.Getter;

// 저장된 경기 영상 파일 정보
// 파일 저장소에 저장된 결과를 경기 영상 서비스에 전달

@Getter
@AllArgsConstructor
public class StoredMatchVideoFile {

	private final String storedFileName;
	private final String accessUrl;
	private final Path storedFilePath;
}
