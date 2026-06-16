package com.soccer.platform.dto.teamanalysisclip;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

/*
 * 팀 분석 클립 페이지 응답 DTO
 * Spring Page 객체를 프론트에 그대로 노출하지 않기 위해 사용한다.
 */
@Getter
@Builder
public class TeamAnalysisClipPageResponseDTO {

    private List<TeamAnalysisClipListResponseDTO> teamAnalysisClips;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static TeamAnalysisClipPageResponseDTO from(Page<TeamAnalysisClipListResponseDTO> teamClipPage) {
        return TeamAnalysisClipPageResponseDTO.builder()
                .teamAnalysisClips(teamClipPage.getContent())
                .page(teamClipPage.getNumber())
                .size(teamClipPage.getSize())
                .totalElements(teamClipPage.getTotalElements())
                .totalPages(teamClipPage.getTotalPages())
                .build();
    }
}