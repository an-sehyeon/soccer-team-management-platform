package com.soccer.platform.entity;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 영상 북마크 Entity
 * 지도자와 분석관이 경기 원본 영상, 팀 분석 클립,
 * 선수 개인 분석 클립에서 다시 확인할 시점을 개인별로 저장
 * 북마크 시간은 현재 북마크 대상 영상 기준 정수 초로 관리
 */
@Entity
@Table(
    name = "video_bookmark",
    indexes = {
        @Index(
            name = "idx_video_bookmark_upload_member_deleted_created",
            columnList = "upload_id, member_id, is_deleted, created_at"
        ),
        @Index(
            name = "idx_video_bookmark_team_clip_member_deleted_created",
            columnList = "team_clip_id, member_id, is_deleted, created_at"
        ),
        @Index(
            name = "idx_video_bookmark_player_clip_member_deleted_created",
            columnList = "player_clip_id, member_id, is_deleted, created_at"
        )
    }
)
@Check(
    constraints = "team_clip_id IS NULL OR player_clip_id IS NULL"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoBookmarkEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /*
     * 북마크가 속한 원본 경기 영상
     * 경기 원본, 팀 분석 클립, 선수 개인 분석 클립 북마크 모두
     * 어느 경기에서 만들어진 데이터인지 확인할 수 있도록 필수로 저장
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    private GameVideoUploadEntity gameVideoUpload;

    /*
     * 북마크 대상 팀 분석 클립
     * 경기 원본 영상 또는 선수 개인 분석 클립 북마크인 경우 null이다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_clip_id")
    private TeamVideoClipEntity teamVideoClip;

    /*
     * 북마크 대상 선수 개인 분석 클립
     * 경기 원본 영상 또는 팀 분석 클립 북마크인 경우 null이다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_clip_id")
    private PlayerVideoClipEntity playerVideoClip;

    /*
     * 북마크 작성자
     * 북마크는 개인 작업 데이터이므로 작성자 본인만
     * 조회, 수정, 삭제할 수 있다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    /*
     * 현재 북마크 대상 영상 기준 시간
     * 경기 원본 영상이면 원본 영상 기준,
     * 분석 클립이면 생성된 클립 영상 내부 기준 초
     */
    @Column(
        name = "bookmark_time_sec",
        nullable = false,
        columnDefinition = "INT UNSIGNED"
    )
    private Integer bookmarkTimeSec;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "memo", length = 255)
    private String memo;

    // 경기 원본 영상 북마크를 생성
    public static VideoBookmarkEntity createMatchVideoBookmark(
        GameVideoUploadEntity gameVideoUpload,
        MemberEntity member,
        Integer bookmarkTimeSec,
        String title,
        String memo
    ) {
        VideoBookmarkEntity bookmark = new VideoBookmarkEntity();

        bookmark.gameVideoUpload = gameVideoUpload;
        bookmark.teamVideoClip = null;
        bookmark.playerVideoClip = null;
        bookmark.member = member;
        bookmark.bookmarkTimeSec = bookmarkTimeSec;
        bookmark.title = title;
        bookmark.memo = memo;
        bookmark.setIsDeleted(false);

        return bookmark;
    }

    // 팀 분석 클립 북마크를 생성
    public static VideoBookmarkEntity createTeamClipBookmark(
        GameVideoUploadEntity gameVideoUpload,
        TeamVideoClipEntity teamVideoClip,
        MemberEntity member,
        Integer bookmarkTimeSec,
        String title,
        String memo
    ) {
        VideoBookmarkEntity bookmark = new VideoBookmarkEntity();

        bookmark.gameVideoUpload = gameVideoUpload;
        bookmark.teamVideoClip = teamVideoClip;
        bookmark.playerVideoClip = null;
        bookmark.member = member;
        bookmark.bookmarkTimeSec = bookmarkTimeSec;
        bookmark.title = title;
        bookmark.memo = memo;
        bookmark.setIsDeleted(false);

        return bookmark;
    }

    // 선수 개인 분석 클립 북마크를 생성
    public static VideoBookmarkEntity createPlayerClipBookmark(
        GameVideoUploadEntity gameVideoUpload,
        PlayerVideoClipEntity playerVideoClip,
        MemberEntity member,
        Integer bookmarkTimeSec,
        String title,
        String memo
    ) {
        VideoBookmarkEntity bookmark = new VideoBookmarkEntity();

        bookmark.gameVideoUpload = gameVideoUpload;
        bookmark.teamVideoClip = null;
        bookmark.playerVideoClip = playerVideoClip;
        bookmark.member = member;
        bookmark.bookmarkTimeSec = bookmarkTimeSec;
        bookmark.title = title;
        bookmark.memo = memo;
        bookmark.setIsDeleted(false);

        return bookmark;
    }

    // 북마크 제목, 메모, 시간을 수정한다.
    // 북마크 대상 영상은 수정할 수 없다.
    public void update(
        Integer bookmarkTimeSec,
        String title,
        String memo
    ) {
        this.bookmarkTimeSec = bookmarkTimeSec;
        this.title = title;
        this.memo = memo;
    }

    // 북마크를 소프트 삭제
    public void softDelete() {
        this.setIsDeleted(true);
    }
}