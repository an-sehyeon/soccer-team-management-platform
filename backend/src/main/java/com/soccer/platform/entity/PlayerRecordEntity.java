package com.soccer.platform.entity;


import com.soccer.platform.dto.playerrecord.CreatePlayerRecordRequestDTO;
import com.soccer.platform.dto.playerrecord.UpdatePlayerRecordRequestDTO;

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

// 선수 기록 Entity

@Entity
@Table(
    name = "player_record",
    indexes = {
        @Index(
            name = "idx_player_record_upload_player_deleted",
            columnList = "upload_id, player_id, is_deleted"
        ),
        @Index(
            name = "idx_player_record_player_deleted",
            columnList = "player_id, is_deleted"
        ),
        @Index(
            name = "idx_player_record_upload_deleted",
            columnList = "upload_id, is_deleted"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerRecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    private GameVideoUploadEntity gameVideoUpload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private MemberEntity player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorder_id", nullable = false)
    private MemberEntity recorder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modifier_id")
    private MemberEntity lastModifier;

    @Column(name = "minutes_played", columnDefinition = "TINYINT UNSIGNED")
    private Integer minutesPlayed;

    @Column(name = "goals", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer goals;

    @Column(name = "assists", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer assists;

    @Column(name = "shots", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer shots;

    @Column(name = "shots_on_target", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer shotsOnTarget;

    @Column(name = "passes", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer passes;

    @Column(name = "successful_passes", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer successfulPasses;

    @Column(name = "dribbles", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer dribbles;

    @Column(name = "successful_dribbles", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer successfulDribbles;

    @Column(name = "tackles", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer tackles;

    @Column(name = "interceptions", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer interceptions;

    @Column(name = "clearances", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer clearances;

    @Column(name = "saves", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer saves;

    @Column(name = "yellow_cards", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer yellowCards;

    @Column(name = "red_cards", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer redCards;

    @Column(name = "memo", length = 255)
    private String memo;

    
    // 선수 기록 생성
    public static PlayerRecordEntity create(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            MemberEntity recorder,
            CreatePlayerRecordRequestDTO request
    ) {
        PlayerRecordEntity playerRecord = new PlayerRecordEntity();

        playerRecord.gameVideoUpload = gameVideoUpload;
        playerRecord.player = player;
        playerRecord.recorder = recorder;
        playerRecord.lastModifier = null;
        playerRecord.minutesPlayed = request.getMinutesPlayed();
        playerRecord.goals = request.getGoals();
        playerRecord.assists = request.getAssists();
        playerRecord.shots = request.getShots();
        playerRecord.shotsOnTarget = request.getShotsOnTarget();
        playerRecord.passes = request.getPasses();
        playerRecord.successfulPasses = request.getSuccessfulPasses();
        playerRecord.dribbles = request.getDribbles();
        playerRecord.successfulDribbles = request.getSuccessfulDribbles();
        playerRecord.tackles = request.getTackles();
        playerRecord.interceptions = request.getInterceptions();
        playerRecord.clearances = request.getClearances();
        playerRecord.saves = request.getSaves();
        playerRecord.yellowCards = request.getYellowCards();
        playerRecord.redCards = request.getRedCards();
        playerRecord.memo = request.getMemo();
        playerRecord.setIsDeleted(false);

        return playerRecord;
    }

    
 // 선수 기록 수정
    public void update(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            MemberEntity lastModifier,
            UpdatePlayerRecordRequestDTO request
    ) {
        this.gameVideoUpload = gameVideoUpload;
        this.player = player;
        this.lastModifier = lastModifier;
        this.minutesPlayed = request.getMinutesPlayed();
        this.goals = request.getGoals();
        this.assists = request.getAssists();
        this.shots = request.getShots();
        this.shotsOnTarget = request.getShotsOnTarget();
        this.passes = request.getPasses();
        this.successfulPasses = request.getSuccessfulPasses();
        this.dribbles = request.getDribbles();
        this.successfulDribbles = request.getSuccessfulDribbles();
        this.tackles = request.getTackles();
        this.interceptions = request.getInterceptions();
        this.clearances = request.getClearances();
        this.saves = request.getSaves();
        this.yellowCards = request.getYellowCards();
        this.redCards = request.getRedCards();
        this.memo = request.getMemo();
    }

    // 선수 기록 소프트 삭제
    public void softDelete() {
        this.setIsDeleted(true);
    }
}