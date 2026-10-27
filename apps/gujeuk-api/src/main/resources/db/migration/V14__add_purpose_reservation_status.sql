SET @reservation_status_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'purpose'
      AND COLUMN_NAME = 'reservation_status'
);

SET @ddl := IF(
    @reservation_status_exists = 0,
    'ALTER TABLE `purpose` ADD COLUMN `reservation_status` TINYINT(1) NOT NULL DEFAULT 0',
    'DO 0'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- A local database may already have the earlier VARCHAR draft of this field.
UPDATE `purpose`
SET `reservation_status` = CASE
    WHEN LOWER(TRIM(CAST(`reservation_status` AS CHAR))) IN ('1', 'true') THEN '1'
    ELSE '0'
END;

ALTER TABLE `purpose`
    MODIFY COLUMN `reservation_status` TINYINT(1) NOT NULL DEFAULT 0;
