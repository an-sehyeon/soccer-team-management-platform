package com.soccer.platform.service.storage;

import org.springframework.web.multipart.MultipartFile;

// 경기 영상 파일 저장소 인터페이스
public interface MatchVideoStorageService {
	
	StoredMatchVideoFile store(MultipartFile videoFile);

}
