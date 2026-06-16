package com.soccer.platform.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.soccer.platform.entity.ScheduleEntity;

/*
 * 스케줄 Repository
 * 스케줄 조회, 상세 조회, 소프트 삭제 여부 확인
 */
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer>{

	// 삭제되지 않은 스케줄 단건 조회
	Optional<ScheduleEntity> findByIdAndIsDeletedFalse(Integer id);
	
	// 삭제되지 않은 전체 스케줄 조회
	// 최신 등록순이 아니라 일정 시간순으로 조회
	List<ScheduleEntity> findByIsDeletedFalseOrderByScheduleDatetimeAsc();
	
	// 특정 기간의 삭제되지 않은 스케줄 조회
	// 월별/주별 스케줄 조회 화면에서 사용
	List<ScheduleEntity> findByScheduleDatetimeBetweenAndIsDeletedFalseOrderByScheduleDatetimeAsc(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
