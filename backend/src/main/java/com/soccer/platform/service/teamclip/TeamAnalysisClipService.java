package com.soccer.platform.service.teamclip;

import java.nio.file.Path;import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipWithDrawingsResponseDTO;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipDrawingItemRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipWithDrawingsResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipListResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipDrawingEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipDrawingRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.teamclipdrawing.TeamAnalysisClipDrawingValidator;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 Service
 *
 * 팀 전체가 볼 수 있는 분석 클립을 등록, 조회, 수정, 삭제한다.
 * 생성 시 실제 mp4 파일을 비동기로 생성
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAnalysisClipService {

    private final TeamVideoClipRepository teamVideoClipRepository;
    private final TeamVideoClipDrawingRepository teamVideoClipDrawingRepository;
    private final TeamAnalysisClipValidator teamAnalysisClipValidator;
    private final TeamAnalysisClipDrawingValidator teamAnalysisClipDrawingValidator;
    private final TeamAnalysisClipLocalFileService teamAnalysisClipLocalFileService;
    private final TeamAnalysisClipGenerationAsyncService teamAnalysisClipGenerationAsyncService;

    // 팀 분석 클립 등록
    @Transactional
    public CreateTeamAnalysisClipResponseDTO createTeamAnalysisClip(
            CustomUserPrincipal principal,
            CreateTeamAnalysisClipRequestDTO request
    ) {
        teamAnalysisClipValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipValidator.validateCreateRequest(request);

        GameVideoUploadEntity matchVideo = teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                request.getMatchVideoId()
        );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        Path originalVideoFilePath = teamAnalysisClipLocalFileService
                .resolveOriginalMatchVideoFilePath(matchVideo);

        MemberEntity editor = teamAnalysisClipValidator.findLoginMember(principal);

        TeamVideoClipEntity teamClip = createProcessingTeamVideoClip(
                matchVideo,
                editor,
                request.getClipType(),
                request.getTitle(),
                request.getComment(),
                request.getStartTimeSec(),
                request.getEndTimeSec()
        );

        TeamVideoClipEntity savedTeamClip = teamVideoClipRepository.save(teamClip);

        TeamAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
                savedTeamClip,
                originalVideoFilePath,
                null
        );

        requestTeamAnalysisClipFileGenerationAfterCommit(generationCommand);

        return new CreateTeamAnalysisClipResponseDTO(
                savedTeamClip.getId(),
                savedTeamClip.getStatus(),
                "팀 분석 클립 파일 생성이 요청되었습니다."
        );
    }

    // 팀 분석 클립과 드로잉 통합 등록
    @Transactional
    public CreateTeamAnalysisClipWithDrawingsResponseDTO createTeamAnalysisClipWithDrawings(
            CustomUserPrincipal principal,
            CreateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
        teamAnalysisClipValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipValidator.validateCreateWithDrawingsRequest(request);

        GameVideoUploadEntity matchVideo = teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                request.getMatchVideoId()
        );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        Path originalVideoFilePath = teamAnalysisClipLocalFileService
                .resolveOriginalMatchVideoFilePath(matchVideo);

        MemberEntity editor = teamAnalysisClipValidator.findLoginMember(principal);

        TeamVideoClipEntity teamClip = createProcessingTeamVideoClip(
                matchVideo,
                editor,
                request.getClipType(),
                request.getTitle(),
                request.getComment(),
                request.getStartTimeSec(),
                request.getEndTimeSec()
        );

        TeamVideoClipEntity savedTeamClip = teamVideoClipRepository.save(teamClip);

        saveDrawingsIfExists(
                request.getDrawings(),
                savedTeamClip,
                editor
        );

        TeamAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
                savedTeamClip,
                originalVideoFilePath,
                null
        );

        requestTeamAnalysisClipFileGenerationAfterCommit(generationCommand);

        return new CreateTeamAnalysisClipWithDrawingsResponseDTO(
                savedTeamClip.getId(),
                savedTeamClip.getStatus(),
                "팀 분석 클립 생성 작업이 등록되었습니다."
        );
    }

    // 팀 분석 클립 목록 조회
    public TeamAnalysisClipPageResponseDTO findTeamAnalysisClips(
            CustomUserPrincipal principal,
            Integer matchVideoId,
            String clipType,
            int page,
            int size
    ) {
        teamAnalysisClipValidator.validateCanRead(principal);

        Pageable pageable = teamAnalysisClipValidator.createDefaultPageable(page, size);
        TeamVideoClipTypeEnum clipTypeEnum = teamAnalysisClipValidator.parseNullableClipType(clipType);
        VideoUploadStatusEnum readyStatus = VideoUploadStatusEnum.READY;

        Page<TeamVideoClipEntity> teamClipPage;

        if (matchVideoId != null && clipTypeEnum != null) {
            teamAnalysisClipValidator.findActiveMatchVideo(matchVideoId);

            teamClipPage = teamVideoClipRepository
                    .findByGameVideoUpload_IdAndClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                            matchVideoId,
                            clipTypeEnum,
                            readyStatus,
                            pageable
                    );
        } else if (matchVideoId != null) {
            teamAnalysisClipValidator.findActiveMatchVideo(matchVideoId);

            teamClipPage = teamVideoClipRepository
                    .findByGameVideoUpload_IdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                            matchVideoId,
                            readyStatus,
                            pageable
                    );
        } else if (clipTypeEnum != null) {
            teamClipPage = teamVideoClipRepository
                    .findByClipTypeAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                            clipTypeEnum,
                            readyStatus,
                            pageable
                    );
        } else {
            teamClipPage = teamVideoClipRepository
                    .findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                            readyStatus,
                            pageable
                    );
        }

        Page<TeamAnalysisClipListResponseDTO> responsePage = teamClipPage
                .map(TeamAnalysisClipListResponseDTO::from);

        return TeamAnalysisClipPageResponseDTO.from(responsePage);
    }

    // 팀 분석 클립 상세 조회
    public TeamAnalysisClipDetailResponseDTO findTeamAnalysisClipDetail(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        teamAnalysisClipValidator.validateCanRead(principal);

        TeamVideoClipEntity teamClip = teamAnalysisClipValidator.findActiveTeamClip(teamClipId);

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

        TeamVideoClipEntity teamClip = teamAnalysisClipValidator.findActiveTeamClip(teamClipId);
        teamAnalysisClipValidator.validateCanUpdateGenerationTarget(teamClip);

        GameVideoUploadEntity matchVideo = teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                teamClip.getGameVideoUpload().getId()
        );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        Path originalVideoFilePath = teamAnalysisClipLocalFileService
                .resolveOriginalMatchVideoFilePath(matchVideo);

        String previousClipUrl = teamClip.getUrl();

        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(teamAnalysisClipValidator.trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());
        teamClip.setUrl(null);
        teamClip.setStatus(VideoUploadStatusEnum.PROCESSING);

        TeamAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
                teamClip,
                originalVideoFilePath,
                previousClipUrl
        );

        requestTeamAnalysisClipFileGenerationAfterCommit(generationCommand);

        return TeamAnalysisClipDetailResponseDTO.from(teamClip);
    }
    
    // 팀 분석 클립과 드로잉 통합 수정
    @Transactional
    public UpdateTeamAnalysisClipWithDrawingsResponseDTO updateTeamAnalysisClipWithDrawings(
            CustomUserPrincipal principal,
            Integer teamClipId,
            UpdateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
        teamAnalysisClipValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipValidator.validateUpdateWithDrawingsRequest(request);

        TeamVideoClipEntity teamClip = teamAnalysisClipValidator.findActiveTeamClip(teamClipId);
        teamAnalysisClipValidator.validateCanUpdateGenerationTarget(teamClip);

        GameVideoUploadEntity matchVideo = teamAnalysisClipValidator.findActiveMatchVideoWithDuration(
                teamClip.getGameVideoUpload().getId()
        );

        teamAnalysisClipValidator.validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        boolean fileGenerationRequired = isTeamClipFileGenerationRequired(
                teamClip,
                request
        );

        Path originalVideoFilePath = null;
        String previousClipUrl = teamClip.getUrl();

        if (fileGenerationRequired) {
            originalVideoFilePath = teamAnalysisClipLocalFileService
                    .resolveOriginalMatchVideoFilePath(matchVideo);
        }

        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(teamAnalysisClipValidator.trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());

        softDeleteExistingDrawings(teamClip);

        MemberEntity editor = teamAnalysisClipValidator.findLoginMember(principal);

        saveDrawingsIfExists(
                request.getDrawings(),
                teamClip,
                editor
        );

        if (fileGenerationRequired) {
            teamClip.setUrl(null);
            teamClip.setStatus(VideoUploadStatusEnum.PROCESSING);

            TeamAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
                    teamClip,
                    originalVideoFilePath,
                    previousClipUrl
            );

            requestTeamAnalysisClipFileGenerationAfterCommit(generationCommand);
        }

        return new UpdateTeamAnalysisClipWithDrawingsResponseDTO(
                teamClip.getId(),
                teamClip.getStatus(),
                fileGenerationRequired,
                fileGenerationRequired
                        ? "팀 분석 클립 수정 후 파일 재생성이 요청되었습니다."
                        : "팀 분석 클립과 드로잉이 수정되었습니다."
        );
    }

    // 팀 분석 클립 삭제
    @Transactional
    public void deleteTeamAnalysisClip(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        teamAnalysisClipValidator.validateCanDelete(principal);

        TeamVideoClipEntity teamClip = teamAnalysisClipValidator.findActiveTeamClip(teamClipId);

        teamClip.setIsDeleted(true);
    }

    // 생성 중 상태의 팀 분석 클립 Entity 생성
    private TeamVideoClipEntity createProcessingTeamVideoClip(
            GameVideoUploadEntity matchVideo,
            MemberEntity editor,
            TeamVideoClipTypeEnum clipType,
            String title,
            String comment,
            Integer startTimeSec,
            Integer endTimeSec
    ) {
        TeamVideoClipEntity teamClip = new TeamVideoClipEntity();

        teamClip.setGameVideoUpload(matchVideo);
        teamClip.setMember(editor);
        teamClip.setClipType(clipType);
        teamClip.setTitle(title.trim());
        teamClip.setComment(teamAnalysisClipValidator.trimNullableText(comment));
        teamClip.setStartTimeSec(startTimeSec);
        teamClip.setEndTimeSec(endTimeSec);
        teamClip.setUrl(null);
        teamClip.setStatus(VideoUploadStatusEnum.PROCESSING);
        teamClip.setIsDeleted(false);

        return teamClip;
    }

    // 팀 분석 클립 파일 생성 명령 생성
    private TeamAnalysisClipGenerationCommand createGenerationCommand(
            TeamVideoClipEntity teamClip,
            Path originalVideoFilePath,
            String previousClipUrl
    ) {
        GeneratedTeamAnalysisClipFile generatedClipFile = teamAnalysisClipLocalFileService
                .createGeneratedClipFile(teamClip.getId());

        return new TeamAnalysisClipGenerationCommand(
                teamClip.getId(),
                originalVideoFilePath,
                generatedClipFile,
                teamClip.getStartTimeSec(),
                teamClip.getEndTimeSec(),
                previousClipUrl
        );
    }

    // 드로잉 목록 저장
    private void saveDrawingsIfExists(
            List<CreateTeamAnalysisClipDrawingItemRequestDTO> drawings,
            TeamVideoClipEntity teamClip,
            MemberEntity editor
    ) {
        if (drawings == null || drawings.isEmpty()) {
            return;
        }

        for (CreateTeamAnalysisClipDrawingItemRequestDTO drawingRequest : drawings) {
            saveDrawing(
                    drawingRequest,
                    teamClip,
                    editor
            );
        }
    }

    // 드로잉 저장
    private void saveDrawing(
            CreateTeamAnalysisClipDrawingItemRequestDTO drawingRequest,
            TeamVideoClipEntity teamClip,
            MemberEntity editor
    ) {
        validateDrawingItemRequest(drawingRequest);

        teamAnalysisClipDrawingValidator.validateDrawingTimeRange(
                drawingRequest.getStartTimeSec(),
                drawingRequest.getEndTimeSec(),
                teamClip
        );

        TeamVideoClipDrawingEntity drawing = new TeamVideoClipDrawingEntity();

        drawing.setTeamVideoClip(teamClip);
        drawing.setMember(editor);
        drawing.setDrawingType(drawingRequest.getDrawingType());
        drawing.setStartTimeSec(drawingRequest.getStartTimeSec());
        drawing.setEndTimeSec(drawingRequest.getEndTimeSec());
        drawing.setDrawingData(
                teamAnalysisClipDrawingValidator.convertDrawingDataToString(
                        drawingRequest.getDrawingData()
                )
        );
        drawing.setIsDeleted(false);

        teamVideoClipDrawingRepository.save(drawing);
    }

    // 통합 등록 드로잉 항목 검증
    private void validateDrawingItemRequest(
            CreateTeamAnalysisClipDrawingItemRequestDTO drawingRequest
    ) {
        if (drawingRequest == null) {
            throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
        }

        if (drawingRequest.getDrawingType() == null) {
            throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
        }

        if (drawingRequest.getStartTimeSec() == null
                || drawingRequest.getEndTimeSec() == null) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
        }
    }

    // 트랜잭션 커밋 후 팀 분석 클립 파일 생성 요청
    private void requestTeamAnalysisClipFileGenerationAfterCommit(
            TeamAnalysisClipGenerationCommand generationCommand
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            teamAnalysisClipGenerationAsyncService.generateTeamAnalysisClipFileAsync(
                    generationCommand
            );
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        teamAnalysisClipGenerationAsyncService.generateTeamAnalysisClipFileAsync(
                                generationCommand
                        );
                    }
                }
        );
    }
    
    // 팀 분석 클립 파일 재생성 필요 여부 판단
    private boolean isTeamClipFileGenerationRequired(
            TeamVideoClipEntity teamClip,
            UpdateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
        if (teamClip.getStatus() == VideoUploadStatusEnum.FAILED) {
            return true;
        }

        if (teamClip.getUrl() == null || teamClip.getUrl().isBlank()) {
            return true;
        }

        return !teamClip.getStartTimeSec().equals(request.getStartTimeSec())
                || !teamClip.getEndTimeSec().equals(request.getEndTimeSec());
    }

    // 기존 팀 분석 드로잉 전체 소프트 삭제
    private void softDeleteExistingDrawings(TeamVideoClipEntity teamClip) {
        List<TeamVideoClipDrawingEntity> existingDrawings = teamVideoClipDrawingRepository
                .findByTeamVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(teamClip);

        for (TeamVideoClipDrawingEntity drawing : existingDrawings) {
            drawing.setIsDeleted(true);
        }
    }
}