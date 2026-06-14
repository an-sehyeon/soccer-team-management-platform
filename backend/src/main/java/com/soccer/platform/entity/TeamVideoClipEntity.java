package com.soccer.platform.entity;

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
 * 팀 분석 클립 엔티티
 *
 * 원본 경기 영상에서 팀 전체에게 공유할 분석 구간을 저장한다.
 * 클립은 실제 파일을 자르지 않고 원본 영상 기준 시작/종료 시간으로 관리한다.
 */
@Entity
@Table(name = "team_video_clip")
@Getter
@Setter
@NoArgsConstructor
public class TeamVideoClipEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    private GameVideoUploadEntity gameVideoUpload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "clip_type", nullable = false, length = 30)
    private String clipType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "comment", length = 255)
    private String comment;

    @Column(name = "start_time_sec", nullable = false)
    private Integer startTimeSec;

    @Column(name = "end_time_sec", nullable = false)
    private Integer endTimeSec;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "UPLOADING";
}
