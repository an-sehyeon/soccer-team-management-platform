package com.soccer.platform.service.teamclip;

import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 생성된 팀 분석 클립 파일 정보
// 파일 생성 결과를 비동기 생성 서비스와 상태 변경 서비스에 전달

@Getter
@AllArgsConstructor
public class GeneratedTeamAnalysisClipFile {

    private final String storedFileName;
    private final String accessUrl;
    private final Path storedFilePath;
}