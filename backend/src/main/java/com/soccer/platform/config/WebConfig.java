package com.soccer.platform.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 웹 리소스 설정 클래스
// 로컬에 저장된 경기 영상 파일을 브라우저에서 접근 가능하도록
// 정적 리소스 경로를 매핑

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.match-video-dir}")
    private String matchVideoUploadDir;

    @Value("${app.upload.match-video-url-prefix}")
    private String matchVideoUrlPrefix;
	
	/*
	 * 업로드된 경기 영상 파일 정적 리소스 매핑
	 * 
	 * 1. application.properties의 로컬 저장 경로를 읽는다
	 * 2. 절대 경로로 변환
	 * 3. /uploads/match-videos/** URL 요청을 로컬 파일 디렉터리와 연결
	 */
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(matchVideoUploadDir)
                .toAbsolutePath()
                .normalize();

        String resourceLocation = uploadPath.toUri().toString();

        registry.addResourceHandler(matchVideoUrlPrefix + "/**")
                .addResourceLocations(resourceLocation + "/");
    }
	
}
