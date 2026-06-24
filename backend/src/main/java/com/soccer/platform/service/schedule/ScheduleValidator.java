package com.soccer.platform.service.schedule;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.soccer.platform.common.constants.ScheduleIntensityEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.schedule.CreateScheduleRequestDTO;
import com.soccer.platform.dto.schedule.UpdateScheduleRequestDTO;

// 스케줄 요청값 Validator
@Component
public class ScheduleValidator {

    // 스케줄 등록 요청값 검증
    public void validateCreateRequest(CreateScheduleRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }

        validateRequiredValues(
                request.getScheduleDateTime(),
                request.getPlace(),
                request.getScheduleType(),
                request.getComment(),
                request.getIntensity()
        );
    }

    // 스케줄 수정 요청값 검증
    public void validateUpdateRequest(UpdateScheduleRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }

        validateRequiredValues(
                request.getScheduleDateTime(),
                request.getPlace(),
                request.getScheduleType(),
                request.getComment(),
                request.getIntensity()
        );
    }

    // 스케줄 조회 기간 검증
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_DATE_RANGE);
        }

        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_DATE_RANGE);
        }
    }

    // 필수값 검증
    private void validateRequiredValues(
            Object scheduleDateTime,
            String place,
            Object scheduleType, 
            String comment, 
            ScheduleIntensityEnum intensity
    ) {
        if (scheduleDateTime == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }

        if (place == null || place.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }
        	
        if (scheduleType == null) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }
        
        if (comment == null) {
        	throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }
        
        if (intensity == null) {
        	throw new CustomException(ErrorCode.INVALID_SCHEDULE_TYPE);
        }
    }
}