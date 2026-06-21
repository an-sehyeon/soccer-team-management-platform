package com.soccer.platform.service.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;

// 페이지 요청 값 공통 Validator
// page, size 검증 기준을 모든 목록 조회 API에서 동일하게 유지
@Component
public class PageRequestValidator {

    private static final int MAX_PAGE_SIZE = 100;

    // 페이지 요청 값 검증
    /*
     * page는 null이 아니어야 한다.
     * page는 0 이상이어야 한다.
     * size는 null이 아니어야 한다.
     * size는 1 이상이어야 한다.
     * size는 최대 100 이하로 제한한다.
     */
    public void validate(Integer page, Integer size) {
        if (page == null || size == null) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    // 검증 후 Pageable을 생성
    public Pageable createPageable(Integer page, Integer size, Sort sort) {
        validate(page, size);

        if (sort == null) {
            return PageRequest.of(page, size);
        }

        return PageRequest.of(page, size, sort);
    }

    // 정렬 조건이 없는 Pageable을 생성
    public Pageable createPageable(Integer page, Integer size) {
        validate(page, size);
        return PageRequest.of(page, size);
    }
}