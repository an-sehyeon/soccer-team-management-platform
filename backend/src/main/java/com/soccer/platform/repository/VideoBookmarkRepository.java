package com.soccer.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soccer.platform.entity.VideoBookmarkEntity;

/*
 * 영상 북마크 Repository
 *
 * 로그인 사용자가 본인이 작성한 북마크만 조회할 수 있도록
 * 작성자 ID와 현재 재생 중인 영상 ID를 함께 조회 조건으로 사용
 *
 * 팀 분석 클립 또는 선수 개인 분석 클립의 시간 구간이 변경되거나
 * 클립이 삭제되면 해당 클립에서 생성한 북마크를 일괄 소프트 삭제
 */
public interface VideoBookmarkRepository
    extends JpaRepository<VideoBookmarkEntity, Integer> {

    /*
     * 작성자 본인의 삭제되지 않은 북마크를 조회
     * 북마크 수정과 삭제 전에 소유권을 확인할 때 사용
     */
    @Query("""
        SELECT bookmark
        FROM VideoBookmarkEntity bookmark
        JOIN FETCH bookmark.gameVideoUpload matchVideo
        LEFT JOIN FETCH bookmark.teamVideoClip teamClip
        LEFT JOIN FETCH bookmark.playerVideoClip playerClip
        WHERE bookmark.id = :bookmarkId
          AND bookmark.member.id = :memberId
          AND bookmark.isDeleted = false
        """)
    Optional<VideoBookmarkEntity> findActiveBookmarkByIdAndMemberId(
        @Param("bookmarkId") Integer bookmarkId,
        @Param("memberId") Integer memberId
    );

    /*
     * 현재 경기 원본 영상에서 작성한 본인의 북마크를 최신 등록순으로 조회
     * 팀 분석 클립 ID와 선수 개인 분석 클립 ID가 모두 없는
     * 경기 원본 영상 북마크만 반환
     */
    @Query("""
        SELECT bookmark
        FROM VideoBookmarkEntity bookmark
        JOIN FETCH bookmark.gameVideoUpload matchVideo
        WHERE matchVideo.id = :matchVideoId
          AND matchVideo.isDeleted = false
          AND bookmark.member.id = :memberId
          AND bookmark.teamVideoClip IS NULL
          AND bookmark.playerVideoClip IS NULL
          AND bookmark.isDeleted = false
        ORDER BY bookmark.createdAt DESC
        """)
    List<VideoBookmarkEntity> findMyMatchVideoBookmarks(
        @Param("matchVideoId") Integer matchVideoId,
        @Param("memberId") Integer memberId
    );

    /*
     * 현재 팀 분석 클립에서 작성한 본인의 북마크를 최신 등록순으로 조회
     * 삭제된 경기 영상이나 삭제된 팀 분석 클립의 북마크는 반환하지 않는다.
     */
    @Query("""
        SELECT bookmark
        FROM VideoBookmarkEntity bookmark
        JOIN FETCH bookmark.gameVideoUpload matchVideo
        JOIN FETCH bookmark.teamVideoClip teamClip
        WHERE matchVideo.id = :matchVideoId
          AND matchVideo.isDeleted = false
          AND teamClip.id = :teamClipId
          AND teamClip.isDeleted = false
          AND bookmark.member.id = :memberId
          AND bookmark.playerVideoClip IS NULL
          AND bookmark.isDeleted = false
        ORDER BY bookmark.createdAt DESC
        """)
    List<VideoBookmarkEntity> findMyTeamClipBookmarks(
        @Param("matchVideoId") Integer matchVideoId,
        @Param("teamClipId") Integer teamClipId,
        @Param("memberId") Integer memberId
    );

    /*
     * 현재 선수 개인 분석 클립에서 작성한 본인의 북마크를 최신 등록순으로 조회
     * 삭제된 경기 영상이나 삭제된 선수 개인 분석 클립의 북마크는 반환하지 않는다.
     */
    @Query("""
        SELECT bookmark
        FROM VideoBookmarkEntity bookmark
        JOIN FETCH bookmark.gameVideoUpload matchVideo
        JOIN FETCH bookmark.playerVideoClip playerClip
        WHERE matchVideo.id = :matchVideoId
          AND matchVideo.isDeleted = false
          AND playerClip.id = :playerClipId
          AND playerClip.isDeleted = false
          AND bookmark.member.id = :memberId
          AND bookmark.teamVideoClip IS NULL
          AND bookmark.isDeleted = false
        ORDER BY bookmark.createdAt DESC
        """)
    List<VideoBookmarkEntity> findMyPlayerClipBookmarks(
        @Param("matchVideoId") Integer matchVideoId,
        @Param("playerClipId") Integer playerClipId,
        @Param("memberId") Integer memberId
    );

    /*
     * 팀 분석 클립에 연결된 활성 북마크를 모두 소프트 삭제
     * 팀 분석 클립의 시작 또는 종료 시간이 변경되거나
     * 팀 분석 클립이 삭제될 때 호출
     * 반환값은 실제로 소프트 삭제된 북마크 개수다.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE VideoBookmarkEntity bookmark
        SET bookmark.isDeleted = true,
            bookmark.updatedAt = CURRENT_TIMESTAMP
        WHERE bookmark.teamVideoClip.id = :teamClipId
          AND bookmark.isDeleted = false
        """)
    int softDeleteActiveBookmarksByTeamClipId(
        @Param("teamClipId") Integer teamClipId
    );

    /*
     * 선수 개인 분석 클립에 연결된 활성 북마크를 모두 소프트 삭제
     * 선수 개인 분석 클립의 시작 또는 종료 시간이 변경되거나
     * 선수 개인 분석 클립이 삭제될 때 호출
     * 반환값은 실제로 소프트 삭제된 북마크 개수다.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE VideoBookmarkEntity bookmark
        SET bookmark.isDeleted = true,
            bookmark.updatedAt = CURRENT_TIMESTAMP
        WHERE bookmark.playerVideoClip.id = :playerClipId
          AND bookmark.isDeleted = false
        """)
    int softDeleteActiveBookmarksByPlayerClipId(
        @Param("playerClipId") Integer playerClipId
    );
}