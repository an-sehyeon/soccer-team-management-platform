package com.soccer.platform.dto.playerclipview;

import java.util.List;

import org.springframework.data.domain.Page;

import com.soccer.platform.entity.PlayerVideoClipViewEntity;

import lombok.Builder;
import lombok.Getter;

/**
 * 선수 개인 분석 클립 조회 기록 페이지 응답 DTO
 *
 * Spring Page 객체를 그대로 노출하지 않고,
 * 프론트에서 필요한 페이징 정보만 반환한다.
 */
@Getter
@Builder
public class PlayerClipViewPageResponseDTO {

    private List<PlayerClipViewResponseDTO> views;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    /**
     * Page<Entity>를 페이지 응답 DTO로 변환
     */
    public static PlayerClipViewPageResponseDTO from(Page<PlayerVideoClipViewEntity> viewPage) {
        return PlayerClipViewPageResponseDTO.builder()
                .views(viewPage.getContent().stream()
                        .map(PlayerClipViewResponseDTO::from)
                        .toList())
                .page(viewPage.getNumber())
                .size(viewPage.getSize())
                .totalElements(viewPage.getTotalElements())
                .totalPages(viewPage.getTotalPages())
                .build();
    }
}