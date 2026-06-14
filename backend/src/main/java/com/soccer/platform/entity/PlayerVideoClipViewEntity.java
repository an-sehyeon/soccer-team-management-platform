package com.soccer.platform.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 선수 개인 분석 클립 조회 이력 엔티티
 *
 * 선수가 개인 분석 클립을 확인했는지 추적한다.
 * 같은 회원이 같은 클립을 조회한 기록은 하나만 유지하고 조회 횟수와 마지막 조회 시간을 갱신한다.
 */
@Entity
@Table(
    name = "player_video_clip_view",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_player_video_clip_view_clip_member",
            columnNames = {"player_video_clip_id", "member_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class PlayerVideoClipViewEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_video_clip_id", nullable = false)
    private PlayerVideoClipEntity playerVideoClip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "first_viewed_at", nullable = false)
    private LocalDateTime firstViewedAt;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
}
