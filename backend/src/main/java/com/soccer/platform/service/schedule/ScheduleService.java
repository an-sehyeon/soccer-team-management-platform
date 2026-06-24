package com.soccer.platform.service.schedule;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.schedule.CreateScheduleRequestDTO;
import com.soccer.platform.dto.schedule.ScheduleResponseDTO;
import com.soccer.platform.dto.schedule.UpdateScheduleRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.ScheduleEntity;
import com.soccer.platform.repository.ScheduleRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * 스케줄 관리 서비스
 *
 * 지도자는 스케줄을 등록, 수정, 삭제 가능
 * 선수와 분석관은 스케줄을 조회만 가능
 */

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final ScheduleValidator scheduleValidator;

    // 스케줄 등록
    public ScheduleResponseDTO createSchedule(
            CustomUserPrincipal principal,
            CreateScheduleRequestDTO request
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.SCHEDULE_ACCESS_DENIED
        );

        scheduleValidator.validateCreateRequest(request);

        MemberEntity writer = memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setMember(writer);
        schedule.setScheduleDatetime(request.getScheduleDateTime());
        schedule.setPlace(request.getPlace());
        schedule.setScheduleType(request.getScheduleType());
        schedule.setIntensity(request.getIntensity());
        schedule.setComment(request.getComment());
        schedule.setIsDeleted(false);

        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);

        return toResponseDTO(savedSchedule);
    }

    // 스케줄 목록 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> findSchedules(
            CustomUserPrincipal principal,
            LocalDate startDate,
            LocalDate endDate
    ) {
    	System.out.println("======서비스 접근 =========");
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.SCHEDULE_ACCESS_DENIED
        );

        scheduleValidator.validateDateRange(startDate, endDate);

        // 스케줄 조회 기간 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ScheduleEntity> schedules = scheduleRepository
                .findByScheduleDatetimeGreaterThanEqualAndScheduleDatetimeLessThanAndIsDeletedFalseOrderByScheduleDatetimeAsc(
                        startDateTime,
                        endDateTime
                );

        return schedules.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 스케줄 상세 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDTO findScheduleDetail(
            CustomUserPrincipal principal,
            Integer scheduleId
    ) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.SCHEDULE_ACCESS_DENIED
        );

        ScheduleEntity schedule = findActiveSchedule(scheduleId);

        return toResponseDTO(schedule);
    }

    // 스케줄 수정
    public ScheduleResponseDTO updateSchedule(
            CustomUserPrincipal principal,
            Integer scheduleId,
            UpdateScheduleRequestDTO request
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.SCHEDULE_ACCESS_DENIED
        );

        scheduleValidator.validateUpdateRequest(request);

        ScheduleEntity schedule = findActiveSchedule(scheduleId);

        schedule.setScheduleDatetime(request.getScheduleDateTime());
        schedule.setPlace(request.getPlace());
        schedule.setScheduleType(request.getScheduleType());
        schedule.setIntensity(request.getIntensity());
        schedule.setComment(request.getComment());

        return toResponseDTO(schedule);
    }

    // 스케줄 삭제
    public void deleteSchedule(
            CustomUserPrincipal principal,
            Integer scheduleId
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.SCHEDULE_ACCESS_DENIED
        );

        ScheduleEntity schedule = findActiveSchedule(scheduleId);

        schedule.setIsDeleted(true);
    }

    // 삭제되지 않은 스케줄 조회
    private ScheduleEntity findActiveSchedule(Integer scheduleId) {
        if (scheduleId == null) {
            throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        return scheduleRepository.findByIdAndIsDeletedFalse(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 스케줄 응답 변환
    private ScheduleResponseDTO toResponseDTO(ScheduleEntity schedule) {
        return ScheduleResponseDTO.from(schedule);
    }
}