package com.soccer.platform.entity;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 원본 경기 영상에서 팀 전체에게 공유할 분석 구간을 저장
 * 클립 생성 시 실제 mp4 파일을 비동기로 생성하고, 생성된 파일 URL을 저장
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

    @Enumerated(EnumType.STRING)
    @Column(name = "clip_type", nullable = false, length = 30)
    private TeamVideoClipTypeEnum clipType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VideoUploadStatusEnum status = VideoUploadStatusEnum.PROCESSING;

    /*
     * 팀 분석 클립 파일 생성 성공 처리
     * FFmpeg 작업 성공 후 생성된 mp4 URL을 저장하고 READY 상태로 변경
     */
    public void markGenerationReady(String generatedClipUrl) {
        this.url = generatedClipUrl;
        this.status = VideoUploadStatusEnum.READY;
    }

    /*
     * 팀 분석 클립 파일 생성 실패 처리
     * FFmpeg 작업 실패 시 FAILED 상태로 변경
     */
    public void markGenerationFailed() {
        this.status = VideoUploadStatusEnum.FAILED;
    }
}