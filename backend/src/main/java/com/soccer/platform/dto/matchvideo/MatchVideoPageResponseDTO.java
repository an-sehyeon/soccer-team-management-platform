package com.soccer.platform.dto.matchvideo;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

/*
 * 경기 영상 목록 페이지 응답 DTO
 * Spring Page 객체를 그대로 노출하지 않고 필요한 페이징 정보만 반환한다.
 */
@Getter
@Builder
public class MatchVideoPageResponseDTO {

    private List<MatchVideoListResponseDTO> matchVideos;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static MatchVideoPageResponseDTO from(Page<MatchVideoListResponseDTO> matchVideoPage) {
        return MatchVideoPageResponseDTO.builder()
                .matchVideos(matchVideoPage.getContent())
                .page(matchVideoPage.getNumber())
                .size(matchVideoPage.getSize())
                .totalElements(matchVideoPage.getTotalElements())
                .totalPages(matchVideoPage.getTotalPages())
                .build();
    }
}