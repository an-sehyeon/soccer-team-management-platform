package com.soccer.platform.entity;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;

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
 * 선수 개인 분석 클립 엔티티
 *
 * 특정 선수 한 명에게 공유할 개인 분석 구간을 저장한다.
 * 지도자는 전체 개인 클립을 볼 수 있고, 선수는 본인 클립만 볼 수 있어야 한다.
 */
@Entity
@Table(name = "player_video_clip")
@Getter
@Setter
@NoArgsConstructor
public class PlayerVideoClipEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    private GameVideoUploadEntity gameVideoUpload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editor_id", nullable = false)
    private MemberEntity editor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private MemberEntity player;

    @Column(name = "clip_type", nullable = false, length = 30)
    private PlayerClipTypeEnum clipType;

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
    private VideoUploadStatusEnum status = VideoUploadStatusEnum.UPLOADING;
}
