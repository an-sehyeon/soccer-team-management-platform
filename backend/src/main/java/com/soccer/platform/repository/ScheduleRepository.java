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
	
	// 기간별 스케줄 조회
	// 삭제되지 않은 스케줄을 시작 일시 이상, 종료 일시 미만 기준으로 조회한다.
	List<ScheduleEntity> findByScheduleDatetimeGreaterThanEqualAndScheduleDatetimeLessThanAndIsDeletedFalseOrderByScheduleDatetimeAsc(
	        LocalDateTime startDateTime,
	        LocalDateTime endDateTime
	);
}
