package com.soccer.platform.controller;

import com.soccer.platform.dto.schedule.CreateScheduleRequestDTO;
import com.soccer.platform.dto.schedule.ScheduleResponseDTO;
import com.soccer.platform.dto.schedule.UpdateScheduleRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 스케줄 관리 Controller
 *
 * 지도자, 선수, 분석관의 스케줄 조회 요청을 처리한다.
 * 스케줄 등록/수정/삭제는 지도자만 가능하다.
 *
 * 권한 기준
 * - 조회: COACH, PLAYER, ANALYST
 * - 등록/수정/삭제: COACH
 *
 * 주의사항
 * - 권한 검증은 프론트가 아니라 ScheduleService에서 다시 처리한다.
 * - 작성자 memberId는 요청값이 아니라 JWT 인증 정보에서 가져온다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService scheduleService;

    /*
     * 스케줄 목록 조회
     *
     * startDateTime과 endDateTime이 없으면 전체 스케줄을 조회한다.
     * 둘 다 있으면 해당 기간의 스케줄만 조회한다.
     *
     * 요청 예시
     * - GET /api/schedules
     * - GET /api/schedules?startDateTime=2026-06-01T00:00:00&endDateTime=2026-07-01T00:00:00
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> findSchedules(
            @RequestParam(name = "startDateTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDateTime,

            @RequestParam(name = "endDateTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDateTime
    ) {
        List<ScheduleResponseDTO> schedules;

        if (startDateTime == null && endDateTime == null) {
            schedules = scheduleService.findSchedules();
        } else {
            schedules = scheduleService.findSchedulesBetween(startDateTime, endDateTime);
        }

        return ResponseEntity.ok(schedules);
    }

    // 스케줄 상세 조회
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponseDTO> findSchedule(
            @PathVariable("scheduleId") Integer scheduleId
    ) {
        ScheduleResponseDTO response = scheduleService.findSchedule(scheduleId);

        return ResponseEntity.ok(response);
    }

    // 스케줄 등록
    @PostMapping("/coach/schedules")
    public ResponseEntity<ScheduleResponseDTO> createSchedule(
            @RequestBody CreateScheduleRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser
    ) {

        ScheduleResponseDTO response = scheduleService.createSchedule(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 스케줄 수정
    @PatchMapping("/coach/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponseDTO> updateSchedule(
            @PathVariable("scheduleId") Integer scheduleId,
            @RequestBody UpdateScheduleRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser
    ) {
        ScheduleResponseDTO response = scheduleService.updateSchedule(scheduleId, request, currentUser);

        return ResponseEntity.ok(response);
    }

    // 스케줄 삭제
    @DeleteMapping("/coach/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable("scheduleId") Integer scheduleId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser
    ) {
        scheduleService.deleteSchedule(scheduleId, currentUser);

        return ResponseEntity.noContent().build();
    }
}