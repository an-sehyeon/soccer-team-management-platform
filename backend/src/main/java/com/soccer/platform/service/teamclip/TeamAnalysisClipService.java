package com.soccer.platform.service.teamclip;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipListResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 Service
 * 원본 경기 영상 기준으로 팀 전체가 볼 수 있는 분석 클립을 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAnalysisClipService {

    private final TeamVideoClipRepository teamVideoClipRepository;
    private final TeamAnalysisClipValidator teamAnalysisClipValidator;

    // 팀 분석 클립 등록
    @Transactional
    public Integer createTeamAnalysisClip(
            CustomUserPrincipal principal,
            CreateTeamAnalysisClipRequestDTO request
    ) {
        teamAnalysisClipValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipValidator.validateCreateRequest(request);

        GameVideoUploadEntity matchVideo =
                teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                        request.getMatchVideoId()
                );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        MemberEntity editor = teamAnalysisClipValidator.findLoginMember(principal);

        TeamVideoClipEntity teamClip = new TeamVideoClipEntity();
        teamClip.setGameVideoUpload(matchVideo);
        teamClip.setMember(editor);
        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(teamAnalysisClipValidator.trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());
        teamClip.setUrl(null);
        teamClip.setStatus(VideoUploadStatusEnum.READY);
        teamClip.setIsDeleted(false);

        TeamVideoClipEntity savedTeamClip = teamVideoClipRepository.save(teamClip);

        return savedTeamClip.getId();
    }

    // 팀 분석 클립 목록 조회
    public TeamAnalysisClipPageResponseDTO findTeamAnalysisClips(
            CustomUserPrincipal principal,
            Integer matchVideoId,
            int page,
            int size
    ) {
        teamAnalysisClipValidator.validateCanRead(principal);

        Page<TeamAnalysisClipListResponseDTO> teamClipPage;

        if (matchVideoId != null) {
            teamAnalysisClipValidator.findActiveMatchVideo(matchVideoId);

            Pageable pageable =
                    teamAnalysisClipValidator.createMatchVideoFilteredPageable(page, size);

            teamClipPage = teamVideoClipRepository
                    .findByGameVideoUpload_IdAndIsDeletedFalse(matchVideoId, pageable)
                    .map(TeamAnalysisClipListResponseDTO::from);
        } else {
            Pageable pageable =
                    teamAnalysisClipValidator.createDefaultPageable(page, size);

            teamClipPage = teamVideoClipRepository
                    .findByIsDeletedFalse(pageable)
                    .map(TeamAnalysisClipListResponseDTO::from);
        }

        return TeamAnalysisClipPageResponseDTO.from(teamClipPage);
    }


    // 팀 분석 클립 상세 조회
    public TeamAnalysisClipDetailResponseDTO findTeamAnalysisClipDetail(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        teamAnalysisClipValidator.validateCanRead(principal);

        TeamVideoClipEntity teamClip =
                teamAnalysisClipValidator.findActiveTeamClip(teamClipId);

        teamAnalysisClipValidator.validateLinkedMatchVideoActive(teamClip);

        return TeamAnalysisClipDetailResponseDTO.from(teamClip);
    }

    // 팀 분석 클립 수정
    @Transactional
    public TeamAnalysisClipDetailResponseDTO updateTeamAnalysisClip(
            CustomUserPrincipal principal,
            Integer teamClipId,
            UpdateTeamAnalysisClipRequestDTO request
    ) {
        teamAnalysisClipValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipValidator.validateUpdateRequest(request);

        TeamVideoClipEntity teamClip =
                teamAnalysisClipValidator.findActiveTeamClip(teamClipId);

        GameVideoUploadEntity matchVideo =
                teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                        teamClip.getGameVideoUpload().getId()
                );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(teamAnalysisClipValidator.trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());

        return TeamAnalysisClipDetailResponseDTO.from(teamClip);
    }


    // 팀 분석 클립 삭제
    @Transactional
    public void deleteTeamAnalysisClip(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        teamAnalysisClipValidator.validateCanDelete(principal);

        TeamVideoClipEntity teamClip =
                teamAnalysisClipValidator.findActiveTeamClip(teamClipId);

        teamClip.setIsDeleted(true);
    }
}