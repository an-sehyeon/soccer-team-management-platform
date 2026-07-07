package com.soccer.platform.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

// 웹 리소스 설정 클래스
// 로컬에 저장된 영상 파일을 브라우저에서 접근 가능하도록 정적 리소스 경로를 매핑

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Value("${app.upload.match-video-dir}")
    private String matchVideoUploadDir;

    @Value("${app.upload.match-video-url-prefix}")
    private String matchVideoUrlPrefix;

    @Value("${app.upload.player-analysis-clip-dir}")
    private String playerAnalysisClipUploadDir;

    @Value("${app.upload.player-analysis-clip-url-prefix}")
    private String playerAnalysisClipUrlPrefix;

    @Value("${app.upload.team-analysis-clip-dir}")
    private String teamAnalysisClipUploadDir;

    @Value("${app.upload.team-analysis-clip-url-prefix}")
    private String teamAnalysisClipUrlPrefix;

    /*
     * 업로드된 영상 파일 정적 리소스 매핑
     * MVP 개발 단계에서는 /uploads/** 직접 접근을 허용한다.
     * 운영 전에는 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addUploadResourceHandler(
                registry,
                matchVideoUrlPrefix,
                matchVideoUploadDir
        );

        addUploadResourceHandler(
                registry,
                playerAnalysisClipUrlPrefix,
                playerAnalysisClipUploadDir
        );

        addUploadResourceHandler(
                registry,
                teamAnalysisClipUrlPrefix,
                teamAnalysisClipUploadDir
        );
    }

    // 업로드 디렉터리 정적 리소스 매핑
    private void addUploadResourceHandler(
            ResourceHandlerRegistry registry,
            String urlPrefix,
            String uploadDir
    ) {
        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        String resourceLocation = uploadPath.toUri().toString();

        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(resourceLocation);
    }
    
    
}