package com.soccer.platform.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보 엔티티
 *
 * 지도자, 선수, 분석관 계정을 관리한다.
 * 회원의 역할과 승인 상태를 기준으로 서비스 접근 권한을 판단한다.
 */
@Entity
@Table(
    name = "member",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_login_id", columnNames = "login_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "login_id", nullable = false, length = 255)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "alma_mater", length = 30)
    private String almaMater;

    @Column(name = "u_number")
    private Integer uniformNumber;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole;

    @Column(name = "is_captain", nullable = false, columnDefinition = "TINYINT")
    private Boolean isCaptain = false;

    @Column(name = "is_admin", nullable = false, columnDefinition = "TINYINT")
    private Boolean isAdmin = false;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "PENDING";
}
