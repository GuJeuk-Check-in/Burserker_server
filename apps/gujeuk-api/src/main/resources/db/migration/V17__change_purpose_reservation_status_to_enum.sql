ALTER TABLE `purpose`
    MODIFY COLUMN `reservation_status` VARCHAR(20) NOT NULL DEFAULT 'NONE';

UPDATE `purpose`
SET `reservation_status` = CASE
    WHEN `reservation_status` IN ('RESERVATION', 'NONE', 'SUB_INCLUDE') THEN `reservation_status`
    WHEN `reservation_status` = '0' THEN 'NONE'
    ELSE 'RESERVATION'
END;
