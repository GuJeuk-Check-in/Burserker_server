-- 데이터는 삭제하지 않는다. 기존 NONE/PENDING 행은 원본 보존용으로 남긴다.
-- 한국 시각으로 저장된 기존 LocalDateTime과 비교하되 MySQL 서버 시간대에는 의존하지 않는다.
SET @beopdong_migration_now = CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00');

DROP PROCEDURE IF EXISTS beopdong_prepare_usage_v2;
DELIMITER $$
CREATE PROCEDURE beopdong_prepare_usage_v2()
BEGIN
    IF (SELECT COUNT(*) FROM organ) > 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Beopdong V2 requires a single institution; review visit ownership first.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = DATABASE()
                 AND table_name IN ('visit_purpose', 'purpose_usage', 'purpose_usage_member')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usage tables already exist; inspect schema before migrating.';
    END IF;

    -- 이전 테이블은 선택 기록만 이전한다. 시간 정보 없는 실제 이용은 추정하지 않는다.
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = DATABASE() AND table_name = 'log_purpose') THEN
        IF EXISTS (SELECT 1 FROM log_purpose WHERE purpose_state <> 'NONE') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Legacy log_purpose contains started records without timestamps; review first.';
        END IF;
    END IF;

    -- 최초 가입용 구조와 신청서 필드를 이미 추가한 구조를 모두 지원한다.
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'name') THEN
        ALTER TABLE purpose_log ADD COLUMN name TEXT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'seat') THEN
        ALTER TABLE purpose_log ADD COLUMN seat INT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'use_purpose') THEN
        ALTER TABLE purpose_log ADD COLUMN use_purpose VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'count') THEN
        ALTER TABLE purpose_log ADD COLUMN `count` INT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'start_at') THEN
        ALTER TABLE purpose_log ADD COLUMN start_at DATETIME(6) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'purpose_log' AND column_name = 'end_at') THEN
        ALTER TABLE purpose_log ADD COLUMN end_at DATETIME(6) NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM purpose_log
               WHERE purpose_state IN ('PENDING', 'SUCCESS')
                 AND (start_at IS NULL OR end_at IS NULL OR end_at < start_at)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Started history has missing or invalid timestamps; review source data.';
    END IF;

    IF EXISTS (SELECT 1 FROM purpose_log pl JOIN purpose p ON p.id = pl.purpose_id
               WHERE pl.purpose_state IN ('PENDING', 'SUCCESS')
                 AND p.purpose_name NOT IN ('인터넷', '노래방', '탁구')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Started history contains an unsupported facility.';
    END IF;

    IF EXISTS (SELECT 1 FROM purpose_log pl JOIN purpose p ON p.id = pl.purpose_id
               WHERE pl.purpose_state = 'PENDING' AND pl.end_at > @beopdong_migration_now
                 AND ((p.purpose_name IN ('노래방', '탁구') AND (pl.`count` IS NULL OR pl.`count` <> 1))
                   OR (p.purpose_name = '인터넷' AND (pl.seat IS NULL OR pl.seat < 1 OR pl.seat > 10)))) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Active legacy group or invalid seat: wait for completion or verify participants.';
    END IF;

    IF EXISTS (SELECT pl.purpose_id, COALESCE(pl.seat, 0)
               FROM purpose_log pl
               WHERE pl.purpose_state = 'PENDING' AND pl.end_at > @beopdong_migration_now
               GROUP BY pl.purpose_id, COALESCE(pl.seat, 0) HAVING COUNT(*) > 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Concurrent legacy facility records overlap; review before migrating.';
    END IF;

    IF EXISTS (SELECT l.user_id FROM purpose_log pl JOIN `log` l ON l.id = pl.log_id
               WHERE pl.purpose_state = 'PENDING' AND pl.end_at > @beopdong_migration_now
               GROUP BY l.user_id HAVING COUNT(*) > 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A legacy user is using several facilities; review before migrating.';
    END IF;
END$$
DELIMITER ;
CALL beopdong_prepare_usage_v2();
DROP PROCEDURE beopdong_prepare_usage_v2;

CREATE TABLE visit_purpose (
    id BIGINT NOT NULL AUTO_INCREMENT,
    log_id BIGINT NOT NULL,
    purpose_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_visit_purpose UNIQUE (log_id, purpose_id),
    CONSTRAINT fk_visit_purpose_log FOREIGN KEY (log_id) REFERENCES `log` (id),
    CONSTRAINT fk_visit_purpose_purpose FOREIGN KEY (purpose_id) REFERENCES purpose (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purpose_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purpose_id BIGINT NOT NULL,
    seat INT NULL,
    use_purpose VARCHAR(255) NULL,
    `count` INT NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    completed BIT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX ix_purpose_usage_expiration (completed, end_at),
    INDEX ix_purpose_usage_seat (purpose_id, seat, completed, end_at),
    CONSTRAINT fk_purpose_usage_purpose FOREIGN KEY (purpose_id) REFERENCES purpose (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purpose_usage_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purpose_usage_id BIGINT NOT NULL,
    log_id BIGINT NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_purpose_usage_member UNIQUE (purpose_usage_id, log_id),
    CONSTRAINT fk_usage_member_usage FOREIGN KEY (purpose_usage_id) REFERENCES purpose_usage (id),
    CONSTRAINT fk_usage_member_log FOREIGN KEY (log_id) REFERENCES `log` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE purpose_log MODIFY COLUMN name TEXT NULL;
ALTER TABLE purpose_log ADD COLUMN purpose_usage_id BIGINT NULL;
ALTER TABLE purpose_log ADD CONSTRAINT uk_purpose_log_usage UNIQUE (purpose_usage_id);
ALTER TABLE purpose_log ADD CONSTRAINT fk_purpose_log_usage
    FOREIGN KEY (purpose_usage_id) REFERENCES purpose_usage (id);

-- 기존 신청서 ID를 새 회차 ID로 사용하여 연결 관계를 명확히 보존한다.
INSERT INTO purpose_usage (id, purpose_id, seat, use_purpose, `count`, start_at, end_at, completed)
SELECT pl.id, pl.purpose_id, pl.seat, pl.use_purpose,
       CASE WHEN p.purpose_name = '인터넷' THEN 1 ELSE COALESCE(pl.`count`, 1) END,
       pl.start_at, pl.end_at,
       (pl.purpose_state = 'SUCCESS' OR pl.end_at <= @beopdong_migration_now)
FROM purpose_log pl JOIN purpose p ON p.id = pl.purpose_id
WHERE pl.purpose_state IN ('PENDING', 'SUCCESS');

-- 옛 count에는 동반자 신원이 없으므로 확인 가능한 신청자만 연결한다.
INSERT INTO purpose_usage_member (purpose_usage_id, log_id, joined_at)
SELECT pl.id, pl.log_id, pl.start_at FROM purpose_log pl
WHERE pl.purpose_state IN ('PENDING', 'SUCCESS');

INSERT INTO visit_purpose (log_id, purpose_id)
SELECT DISTINCT log_id, purpose_id FROM purpose_log;

-- 옛 log_purpose가 있는 DB의 선택도 누락하지 않는다. 없는 DB에서는 아무 작업도 하지 않는다.
SET @beopdong_copy_legacy = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'log_purpose'),
    'INSERT INTO visit_purpose (log_id, purpose_id) SELECT DISTINCT old_record.log_id, old_record.purpose_id FROM log_purpose old_record WHERE NOT EXISTS (SELECT 1 FROM visit_purpose vp WHERE vp.log_id = old_record.log_id AND vp.purpose_id = old_record.purpose_id)',
    'SELECT 1'
);
PREPARE beopdong_copy_legacy_statement FROM @beopdong_copy_legacy;
EXECUTE beopdong_copy_legacy_statement;
DEALLOCATE PREPARE beopdong_copy_legacy_statement;

INSERT INTO purpose (purpose_name, purpose_index, organ_id, locked)
SELECT '휴식', COALESCE((SELECT MAX(p.purpose_index) FROM purpose p WHERE p.organ_id = o.id), 0) + 1, o.id, TRUE
FROM organ o
WHERE NOT EXISTS (SELECT 1 FROM purpose p WHERE p.organ_id = o.id AND p.purpose_name = '휴식');
UPDATE purpose SET locked = TRUE WHERE purpose_name = '휴식';

INSERT INTO visit_purpose (log_id, purpose_id)
SELECT l.id, rest.id FROM `log` l
CROSS JOIN (SELECT MIN(id) AS id FROM purpose WHERE purpose_name = '휴식') rest
WHERE rest.id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM visit_purpose vp WHERE vp.log_id = l.id AND vp.purpose_id = rest.id);

UPDATE purpose_log pl JOIN `log` l ON l.id = pl.log_id
SET pl.name = l.name WHERE pl.name IS NULL;

-- 이미 끝난 기록은 기존 PK·수정 이름·인원수를 보존한다.
UPDATE purpose_log pl JOIN purpose_usage usage_record ON usage_record.id = pl.id
SET pl.purpose_state = 'SUCCESS', pl.purpose_usage_id = usage_record.id
WHERE usage_record.completed = TRUE;

-- 아직 사용 중인 옛 PENDING 행은 보존한다. 종료 서비스가 새 완료 이력을 한 번 생성한다.
