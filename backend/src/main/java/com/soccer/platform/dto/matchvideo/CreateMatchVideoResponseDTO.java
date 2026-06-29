package com.soccer.platform.dto.matchvideo;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 경기 영상 등록 응답 DTO

@Getter
@AllArgsConstructor
public class CreateMatchVideoResponseDTO {

    private Integer matchVideoId;
    private String message;

    public static CreateMatchVideoResponseDTO of(Integer matchVideoId) {
        return new CreateMatchVideoResponseDTO(
                matchVideoId,
                "경기 영상이 업로드되었습니다."
        );
    }
}