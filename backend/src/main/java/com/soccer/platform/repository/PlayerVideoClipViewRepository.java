package com.soccer.platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.PlayerVideoClipViewEntity;

// 선수 클립 조회 기록 Repository

public interface PlayerVideoClipViewRepository extends JpaRepository<PlayerVideoClipViewEntity, Integer>{
	
	// 특정 선수가 특정 개인 분석 클립을 조회한 기록 조회
	// 선수가 본인 개인 분석 클립 상세 조회 시 사용
	// 기존 기록이 있으면 lastViewedAt 갱신 및 viewCount 증가
	// 기존 기록이 없으면 최초 조회 기록 생성
	Optional<PlayerVideoClipViewEntity> findByPlayerVideoClipAndMember(
            PlayerVideoClipEntity playerVideoClip,
            MemberEntity member
    );
	
	// 특정 선수의 개인 분석 클립 조회 기록 목록 조회
	// 지도자/분석관이 특정 선수의 피드백 확인 상태를 볼 때 사용
    // 선수가 본인 조회 기록을 볼 때 사용
	Page<PlayerVideoClipViewEntity> findByMemberAndIsDeletedFalse(
            MemberEntity member,
            Pageable pageable
    );

}
