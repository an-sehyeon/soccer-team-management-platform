CREATE DATABASE IF NOT EXISTS soccer_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

use resume_test;

CREATE TABLE `member` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `login_id` VARCHAR(255) NOT NULL COMMENT '회원 로그인 ID',
    `password` VARCHAR(255) NOT NULL COMMENT '비밀번호',
    `phone` VARCHAR(20) NOT NULL COMMENT '휴대폰 번호',
    `name` VARCHAR(20) NOT NULL COMMENT '회원 이름',
    `grade` INT NULL COMMENT '학년',
    `alma_mater` VARCHAR(30) NULL COMMENT '출신학교',
    `u_number` TINYINT UNSIGNED NULL COMMENT '등번호',
    `last_login_at` DATETIME NULL COMMENT '마지막 로그인 시간',
    `member_role` VARCHAR(20) NOT NULL COMMENT '직책: COACH, PLAYER, ANALYST',
    `is_captain` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '주장 여부: 0 아님, 1 주장',
    `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '관리자 권한 여부: 0 일반, 1 관리자',
    `approval_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_login_id` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `schedule` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `member_id` INT NOT NULL COMMENT '회원 ID',
    `schedule_datetime` DATETIME NOT NULL COMMENT '스케줄 날짜와 시간',
    `place` VARCHAR(30) NOT NULL COMMENT '장소',
    `schedule_type` VARCHAR(20) NOT NULL COMMENT 'TRAINING, MATCH, MEETING, EVENT, EXTERNAL, ETC',
    `comment` VARCHAR(255) NULL COMMENT '스케줄 상세 내용',
    `intensity` VARCHAR(10) NULL COMMENT 'HIGH, MEDIUM, LOW',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_schedule_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notice` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `member_id` INT NOT NULL COMMENT '회원 ID',
    `title` VARCHAR(255) NOT NULL COMMENT '공지사항 제목',
    `content` TEXT NOT NULL COMMENT '공지사항 내용',
    `is_important` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '일반: 0, 중요: 1',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notice_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `game_video_upload` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `member_id` INT NOT NULL COMMENT '회원 ID',
    `url` VARCHAR(255) NOT NULL COMMENT '실제 영상 접근 URL',
    `title` VARCHAR(255) NOT NULL COMMENT '경기 제목',
    `game_date` DATETIME NOT NULL COMMENT '경기 한 날짜',
    `place` VARCHAR(255) NOT NULL COMMENT '장소',
    `home_score` TINYINT UNSIGNED NOT NULL COMMENT '홈팀 득점',
    `away_score` TINYINT UNSIGNED NOT NULL COMMENT '원정팀 득점',
    `match_result` VARCHAR(5) NOT NULL COMMENT 'WIN, DRAW, LOSS',
    `status` VARCHAR(20) NOT NULL DEFAULT 'UPLOADING' COMMENT 'UPLOADING, READY, FAILED',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_game_video_upload_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `team_video_clip` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `upload_id` INT NOT NULL COMMENT '경기 영상 업로드 ID',
    `member_id` INT NOT NULL COMMENT '회원 ID',
    `clip_type` VARCHAR(30) NOT NULL COMMENT 'HIGHLIGHT, ATTACK, DEFENSE, GOAL, CONCEDED, OFFSIDE, SETPIECE, ETC',
    `title` VARCHAR(255) NOT NULL COMMENT '편집 클립 제목',
    `comment` VARCHAR(255) null COMMENT '편집 클립에 대한 설명',
    `start_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 시작 시간(초)',
    `end_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 종료 시간(초)',
    `url` VARCHAR(255) NULL COMMENT '편집된 클립 영상 URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'READY' COMMENT 'READY, PROCESSING, FAILED',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_team_video_clip_upload`
        FOREIGN KEY (`upload_id`) REFERENCES `game_video_upload` (`id`),
    CONSTRAINT `fk_team_video_clip_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `player_video_clip` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `upload_id` INT NOT NULL COMMENT '경기 영상 업로드 ID',
    `editor_id` INT NOT NULL COMMENT '편집한 회원 ID',
    `player_id` INT NOT NULL COMMENT '영상 대상 선수 회원 ID',
    `clip_type` VARCHAR(30) NOT NULL COMMENT 'PLAYER_GOOD, PLAYER_MISTAKE, SHOOTING, PASS, DRIBBLE, DEFENSE, POSITIONING, PRESSING, OFF_THE_BALL, ETC',
    `title` VARCHAR(255) NOT NULL COMMENT '개인 분석 영상 제목',
    `comment` VARCHAR(255) null COMMENT '편집 클립에 대한 설명',
    `start_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 시작 시간(초)',
    `end_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 종료 시간(초)',
    `url` VARCHAR(255) NULL COMMENT '편집된 클립 영상 URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'READY' COMMENT 'READY, PROCESSING, FAILED',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_player_video_clip_upload`
        FOREIGN KEY (`upload_id`) REFERENCES `game_video_upload` (`id`),
    CONSTRAINT `fk_player_video_clip_editor`
        FOREIGN KEY (`editor_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_player_video_clip_player`
        FOREIGN KEY (`player_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `player_video_clip_view` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `player_video_clip_id` INT NOT NULL COMMENT '개인 영상 클립 ID',
    `member_id` INT NOT NULL COMMENT '영상 조회한 ID',
    `first_viewed_at` DATETIME NOT NULL COMMENT '최초 조회 시간',
    `last_viewed_at` DATETIME NOT NULL COMMENT '마지막 조회 시간',
    `view_count` INT UNSIGNED NOT NULL COMMENT '조회 횟수',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_player_video_clip_view_clip_member` (`player_video_clip_id`, `member_id`),
    CONSTRAINT `fk_player_video_clip_view_clip`
        FOREIGN KEY (`player_video_clip_id`) REFERENCES `player_video_clip` (`id`),
    CONSTRAINT `fk_player_video_clip_view_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `team_video_clip_drawing` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `team_video_clip_id` INT NOT NULL COMMENT '팀 영상 클립 ID',
    `member_id` INT NOT NULL COMMENT '드로잉 작성 회원 ID',
    `drawing_type` VARCHAR(20) NOT NULL COMMENT 'LINE, ARROW, CIRCLE, BOX, AREA, TEXT',
    `start_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 드로잉 표시 시작 시간(초)',
    `end_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 드로잉 표시 종료 시간(초)',
    `drawing_data` JSON NOT NULL COMMENT '좌표, 색상, 두께, 텍스트 등 드로잉 데이터',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_team_video_clip_drawing_clip`
        FOREIGN KEY (`team_video_clip_id`) REFERENCES `team_video_clip` (`id`),
    CONSTRAINT `fk_team_video_clip_drawing_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `player_video_clip_drawing` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `player_video_clip_id` INT NOT NULL COMMENT '선수 개인 영상 클립 ID',
    `member_id` INT NOT NULL COMMENT '드로잉 작성 회원 ID',
    `drawing_type` VARCHAR(20) NOT NULL COMMENT 'LINE, ARROW, CIRCLE, BOX, AREA, TEXT',
    `start_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 드로잉 표시 시작 시간(초)',
    `end_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 드로잉 표시 종료 시간(초)',
    `drawing_data` JSON NOT NULL COMMENT '좌표, 색상, 두께, 텍스트 등 드로잉 데이터',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_player_video_clip_drawing_clip`
        FOREIGN KEY (`player_video_clip_id`) REFERENCES `player_video_clip` (`id`),
    CONSTRAINT `fk_player_video_clip_drawing_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 드로잉 기능 적용 방식
-- 화살표 → drawing_type = 'ARROW'
-- 원 표시 → drawing_type = 'CIRCLE'
-- 박스 표시 → drawing_type = 'BOX'
-- 영역 표시 → drawing_type = 'AREA'
-- 영상 위 텍스트 코멘트 → drawing_type = 'TEXT'

-- 예를 들어 지도자가 원본 영상 755초부터 760초까지 “여기서 전진 패스 가능”이라는 텍스트를 띄우려면 이렇게 저장
-- drawing_type = TEXT
-- start_time_sec = 755
-- end_time_sec = 760
-- drawing_data = {
--   "x": 0.35,
--   "y": 0.22,
--   "text": "여기서 전진 패스 가능",
--   "color": "#ffffff",
--   "fontSize": 18
-- }


CREATE TABLE `video_bookmark` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `upload_id` INT NOT NULL COMMENT '경기 영상 업로드 ID',
    `member_id` INT NOT NULL COMMENT '북마크 작성 회원 ID',
    `bookmark_time_sec` INT UNSIGNED NOT NULL COMMENT '원본 영상 기준 북마크 시간(초)',
    `title` VARCHAR(255) NOT NULL COMMENT '북마크 제목',
    `memo` VARCHAR(255) NULL COMMENT '북마크 메모',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_video_bookmark_upload`
        FOREIGN KEY (`upload_id`) REFERENCES `game_video_upload` (`id`),
    CONSTRAINT `fk_video_bookmark_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;






