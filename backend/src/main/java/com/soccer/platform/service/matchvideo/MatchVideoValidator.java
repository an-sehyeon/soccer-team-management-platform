package com.soccer.platform.service.matchvideo;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.constants.MatchResultEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
import com.soccer.platform.dto.matchvideo.UpdateMatchVideoRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.PageRequestValidator;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 경기 영상 Validator
 경기 영상 기능에서 사용하는 권한 검증, 요청값 검증, Entity 조회를 담당
 - 경기 영상 등록/수정 권한 검증
 - 경기 영상 조회 권한 검증
 - 경기 영상 삭제 권한 검증
 - 경기 영상 등록/수정 메타데이터 검증
 - 경기 영상 길이 검증
 - 삭제되지 않은 경기 영상 조회
 - 로그인 회원 조회

 주의사항
 - 실제 영상 파일 검증은 MatchVideoStorageService 구현체에서 처리
 - 이 Validator는 경기 제목, 경기일, 장소, 점수, 경기 결과 같은 DB 저장 메타데이터 검증만 담당
 */
@Component
@RequiredArgsConstructor
public class MatchVideoValidator {

    private static final int MAX_TINYINT_UNSIGNED_VALUE = 255;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_PLACE_LENGTH = 255;

    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MemberRepository memberRepository;
    private final PermissionValidator permissionValidator;
    private final PageRequestValidator pageRequestValidator;

    // 경기 영상 등록/수정 권한 검증
    public void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.MATCH_VIDEO_ACCESS_DENIED
        );
    }

    // 경기 영상 관리 권한 검증
    // 등록/수정 권한과 같은 정책을 사용하기 위한 별도 이름의 메서드
    public void validateCanManageMatchVideo(CustomUserPrincipal principal) {
        validateCanCreateOrUpdate(principal);
    }

    // 경기 영상 조회 권한 검증
    public void validateCanRead(CustomUserPrincipal principal) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.MATCH_VIDEO_ACCESS_DENIED
        );
    }

    // 경기 영상 삭제 권한 검증
    public void validateCanDelete(CustomUserPrincipal principal) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.MATCH_VIDEO_ACCESS_DENIED
        );
    }

    // 로그인 회원 조회
    public MemberEntity findLoginMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 삭제되지 않은 경기 영상 조회
    public GameVideoUploadEntity findActiveMatchVideo(Integer matchVideoId) {
        if (matchVideoId == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(matchVideoId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 경기 영상 목록 Pageable 생성
    public Pageable createMatchVideoPageable(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "gameDate")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));

        return pageRequestValidator.createPageable(page, size, sort);
    }

    // 경기 영상 등록 요청 검증
    // multipart 요청에서 영상 파일을 제외한 경기 메타데이터만 검증
    public void validateCreateRequest(CreateMatchVideoRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateMatchVideoRequiredValues(
                request.getTitle(),
                request.getGameDate(),
                request.getPlace(),
                request.getHomeScore(),
                request.getAwayScore(),
                request.getMatchResult()
        );
    }

    // 경기 영상 수정 요청 검증
    // 이번 MVP에서는 영상 파일 교체 없이 경기 메타데이터만 수정
    public void validateUpdateRequest(UpdateMatchVideoRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateMatchVideoRequiredValues(
                request.getTitle(),
                request.getGameDate(),
                request.getPlace(),
                request.getHomeScore(),
                request.getAwayScore(),
                request.getMatchResult()
        );
    }

    // 경기 영상 공통 필수값 검증
    private void validateMatchVideoRequiredValues(
            String title,
            LocalDateTime gameDate,
            String place,
            Integer homeScore,
            Integer awayScore,
            MatchResultEnum matchResult
    ) {
        validateRequiredText(title);
        validateRequiredText(place);

        if (gameDate == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        if (matchResult == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateScore(homeScore);
        validateScore(awayScore);

        validateMaxLength(title, MAX_TITLE_LENGTH);
        validateMaxLength(place, MAX_PLACE_LENGTH);
    }

    // 필수 문자열 검증
    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 문자열 길이 검증
    private void validateMaxLength(String value, int maxLength) {
        if (value.trim().length() > maxLength) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 경기 점수 검증
    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 원본 경기 영상 길이 검증
    public void validateDurationSec(Integer durationSec) {
        if (durationSec == null || durationSec <= 0) {
            throw new CustomException(ErrorCode.INVALID_VIDEO_DURATION);
        }
    }
}