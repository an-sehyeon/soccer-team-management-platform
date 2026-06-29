package com.soccer.platform.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;

import jakarta.annotation.PostConstruct;

// 로컬 경기 영상 파일 저장 서비스
// 개발 단계에서 업로드된 경기 영상을 서버 로컬 디렉터리에 저장
// 추후 S3 전환 시 이 구현체를 S3구현체로 교체
@Service
public class LocalMatchVideoStorageService implements MatchVideoStorageService {

	private static final String ALLOWED_EXTENSION = "mp4";
    private static final String ALLOWED_CONTENT_TYPE = "video/mp4";
    
    @Value("${app.upload.match-video-dir}")
    private String matchVideoUploadDir;

    @Value("${app.upload.match-video-url-prefix}")
    private String matchVideoUrlPrefix;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    private Path uploadPath;
    
    // 업로드 디렉터리 초기화
    @PostConstruct
    public void initializeUploadDirectory() {
        try {
            this.uploadPath = Paths.get(matchVideoUploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_UPLOAD_DIRECTORY_CREATE_FAILED);
        }
    }
    
    /*
     * 경기 영상 파일 저장
     * 1. 파일 존재 여부 검증
     * 2. 파일 확장자와 MIME 타입 검증
     * 3. UUID 기반 저장 파일명 생성
     * 4. 로컬 디렉터리에 파일 저장
     * 5. DB 저장용 접근 URL과 실제 저장 경로 반환
     */
    @Override
    public StoredMatchVideoFile store(MultipartFile videoFile) {
        validateVideoFile(videoFile);

        String storedFileName = createStoredFileName(videoFile);
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        try {
            Files.copy(videoFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_FILE_SAVE_FAILED);
        }

        String accessUrl = matchVideoUrlPrefix + "/" + storedFileName;

        return new StoredMatchVideoFile(storedFileName, accessUrl, targetPath);
    } 
    
    // 업로드 파일 검증
    private void validateVideoFile(MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_FILE_REQUIRED);
        }

        if (videoFile.getSize() > maxFileSize.toBytes()) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_FILE_SIZE_EXCEEDED);
        }

        String originalFilename = videoFile.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || !ALLOWED_EXTENSION.equals(extension.toLowerCase(Locale.ROOT))) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_FILE_EXTENSION);
        }

        String contentType = videoFile.getContentType();

        if (!ALLOWED_CONTENT_TYPE.equals(contentType)) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_CONTENT_TYPE);
        }
    }
    
    // 저장 파일명 생성
    private String createStoredFileName(MultipartFile videoFile) {
        String originalFilename = videoFile.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        return UUID.randomUUID() + "." + extension.toLowerCase(Locale.ROOT);
    }
    
}