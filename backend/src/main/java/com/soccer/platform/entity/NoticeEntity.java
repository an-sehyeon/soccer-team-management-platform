package com.soccer.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 엔티티
 *
 * 지도자가 등록한 팀 공지사항을 관리한다.
 * 중요 공지 여부를 함께 저장해서 화면에서 우선 노출할 수 있다.
 */
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
public class NoticeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_important", nullable = false, columnDefinition = "TINYINT")
    private Boolean isImportant = false;
    
    
    // 공지사항 생성
    public NoticeEntity(MemberEntity member, String title, String content, Boolean isImportant) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null && isImportant;
    }

    // 공지사항 수정
    public void updateNotice(String title, String content, Boolean isImportant) {
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null && isImportant;
    }
}
