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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 경기 영상 업로드 엔티티
 *
 * 지도자가 업로드한 원본 경기 영상 정보를 관리한다.
 * 팀 분석 클립, 선수 개인 클립, 북마크는 이 원본 영상을 기준으로 생성된다.
 */
@Entity
@Table(name = "game_video_upload")
@Getter
@Setter
@NoArgsConstructor
public class GameVideoUploadEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "game_date", nullable = false)
    private LocalDateTime gameDate;

    @Column(name = "place", nullable = false, length = 255)
    private String place;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore;

    @Column(name = "match_result", nullable = false, length = 5)
    private String matchResult;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "UPLOADING";
}
