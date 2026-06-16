package com.soccer.platform.service;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.schedule.CreateScheduleRequestDTO;
import com.soccer.platform.dto.schedule.ScheduleResponseDTO;
import com.soccer.platform.dto.schedule.UpdateScheduleRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.ScheduleEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.ScheduleRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 스케줄 관리 서비스
 *
 * 지도자는 스케줄을 등록, 수정, 삭제할 수 있다.
 * 선수와 분석관은 스케줄을 조회만 할 수 있다.
 *
 * 권한 기준
 * - 등록/수정/삭제: COACH
 * - 조회: COACH, PLAYER, ANALYST
 *
 * - memberId는 요청값이 아니라 JWT 인증 정보에서 가져온다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    /*
     * 전체 스케줄 조회
     *
     * 삭제되지 않은 스케줄을 일정 시간 오름차순으로 조회한다.
     * 지도자, 선수, 분석관 모두 조회할 수 있다.
     */
    public List<ScheduleResponseDTO> findSchedules() {
        return scheduleRepository.findByIsDeletedFalseOrderByScheduleDatetimeAsc()
                .stream()
                .map(ScheduleResponseDTO::from)
                .toList();
    }

    /*
     * 기간별 스케줄 조회
     *
     * 월별/주별 스케줄 화면에서 사용한다.
     * startDateTime과 endDateTime 사이의 삭제되지 않은 스케줄만 조회한다.
     */
    public List<ScheduleResponseDTO> findSchedulesBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        validateScheduleSearchDateTime(startDateTime, endDateTime);

        return scheduleRepository.findByScheduleDatetimeBetweenAndIsDeletedFalseOrderByScheduleDatetimeAsc(
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(ScheduleResponseDTO::from)
                .toList();
    }

    /*
     * 스케줄 상세 조회
     *
     * 삭제되지 않은 스케줄만 조회한다.
     * 지도자, 선수, 분석관 모두 조회할 수 있다.
     */
    public ScheduleResponseDTO findSchedule(Integer scheduleId) {
        ScheduleEntity schedule = findScheduleEntity(scheduleId);

        return ScheduleResponseDTO.from(schedule);
    }

    /*
     * 스케줄 등록
     * - 작성자 memberId는 요청 DTO가 아니라 JWT 인증 정보에서 가져온다.
     */
    @Transactional
    public ScheduleResponseDTO createSchedule(
            CreateScheduleRequestDTO request,
            CustomUserPrincipal currentUser
    ) {
        checkCoachPermission(currentUser);

        MemberEntity writer = findLoginMember(currentUser.getMemberId());

        validateCreateScheduleRequest(request);

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setMember(writer);
        schedule.setScheduleDatetime(request.getScheduleDatetime());
        schedule.setPlace(request.getPlace().trim());
        schedule.setScheduleType(request.getScheduleType());
        schedule.setComment(trimToNull(request.getComment()));
        schedule.setIntensity(request.getIntensity());

        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);

        return ScheduleResponseDTO.from(savedSchedule);
    }

    /*
     * 스케줄 수정
     * - PATCH 방식의 부분 수정을 기준으로 한다.
     * - null 값은 기존 값을 유지한다.
     */
    @Transactional
    public ScheduleResponseDTO updateSchedule(
            Integer scheduleId,
            UpdateScheduleRequestDTO request,
            CustomUserPrincipal currentUser
    ) {
        checkCoachPermission(currentUser);

        ScheduleEntity schedule = findScheduleEntity(scheduleId);

        updateScheduleFields(schedule, request);

        return ScheduleResponseDTO.from(schedule);
    }

    /*
     * 스케줄 삭제
     *
     * 실제 DB row를 삭제하지 않고 isDeleted 값을 true로 변경한다.
     */
    @Transactional
    public void deleteSchedule(
            Integer scheduleId,
            CustomUserPrincipal currentUser
    ) {
        checkCoachPermission(currentUser);

        ScheduleEntity schedule = findScheduleEntity(scheduleId);

        schedule.setIsDeleted(true);
    }

    /*
     * 지도자 권한 확인
     *
     * 스케줄 등록/수정/삭제는 COACH만 가능하다.
     * isAdmin 여부로 판단하지 않는다.
     */
    private void checkCoachPermission(CustomUserPrincipal currentUser) {
        if (!MemberRoleEnum.COACH.equals(currentUser.getMemberRole())) {
            throw new CustomException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }
    }

    /*
     * 로그인 회원 조회
     *
     * JWT에 들어있는 memberId가 실제 DB에 존재하고 삭제되지 않은 회원인지 확인한다.
     */
    private MemberEntity findLoginMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 삭제되지 않은 스케줄 조회
    private ScheduleEntity findScheduleEntity(Integer scheduleId) {
        return scheduleRepository.findByIdAndIsDeletedFalse(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 스케줄 등록 요청값 검증
    private void validateCreateScheduleRequest(CreateScheduleRequestDTO request) {
        if (request.getScheduleDatetime() == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_DATETIME);
        }

        if (!StringUtils.hasText(request.getPlace())) {
            throw new CustomException(ErrorCode.REQUIRED_SCHEDULE_PLACE);
        }

        if (request.getScheduleType() == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }

        if (request.getIntensity() != null && request.getScheduleType() == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }
    }

    //  기간 조회 날짜 검증
    private void validateScheduleSearchDateTime(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        if (startDateTime == null || endDateTime == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_DATETIME);
        }

        if (!startDateTime.isBefore(endDateTime)) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_DATETIME);
        }
    }

    //  스케줄 수정값 반영
    private void updateScheduleFields(
            ScheduleEntity schedule,
            UpdateScheduleRequestDTO request
    ) {
        if (request.getScheduleDatetime() != null) {
            schedule.setScheduleDatetime(request.getScheduleDatetime());
        }

        if (StringUtils.hasText(request.getPlace())) {
            schedule.setPlace(request.getPlace().trim());
        }

        if (request.getScheduleType() != null) {
            schedule.setScheduleType(request.getScheduleType());
        }

        if (request.getComment() != null) {
            schedule.setComment(trimToNull(request.getComment()));
        }

        if (request.getIntensity() != null) {
            schedule.setIntensity(request.getIntensity());
        }
    }

    //  공백 문자열을 null로 변환한다.
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}