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
 * 영상 북마크 엔티티
 *
 * 지도자가 원본 경기 영상에서 다시 확인해야 할 시점을 저장한다.
 * 북마크 시간은 원본 경기 영상 기준 초 단위로 관리한다.
 */
@Entity
@Table(name = "video_bookmark")
@Getter
@Setter
@NoArgsConstructor
public class VideoBookmarkEntity extends BaseEntity {

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

    @Column(name = "bookmark_time_sec", nullable = false)
    private Integer bookmarkTimeSec;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "memo", length = 255)
    private String memo;
}
