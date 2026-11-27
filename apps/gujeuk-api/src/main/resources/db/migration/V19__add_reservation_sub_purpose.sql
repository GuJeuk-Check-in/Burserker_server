-- 하위 방문목적 단위 예약을 위해 예약에 하위 방문목적을 연결한다.
-- purpose_id는 항상 상위 방문목적을 가리키고, 하위 예약이면 sub_purpose_id도 채운다.
-- MySQL 유니크 인덱스는 NULL끼리 중복으로 보지 않으므로, NULL을 0으로 바꾼 생성 컬럼을 슬롯 키에 넣는다.
ALTER TABLE `purpose_reservation`
    ADD COLUMN `sub_purpose_id` BIGINT NULL AFTER `purpose_id`,
    ADD COLUMN `slot_sub_purpose_id` BIGINT AS (IFNULL(`sub_purpose_id`, 0)) STORED AFTER `sub_purpose_id`,
    ADD CONSTRAINT `fk_reservation_sub_purpose` FOREIGN KEY (`sub_purpose_id`) REFERENCES `sub_purpose` (`id`),
    ADD CONSTRAINT `uk_reservation_unit_slot`
        UNIQUE (`purpose_id`, `slot_sub_purpose_id`, `reservation_date`, `start_time`);

-- fk_reservation_purpose가 쓸 인덱스(purpose_id로 시작)를 먼저 만든 뒤 기존 슬롯 키를 지운다.
ALTER TABLE `purpose_reservation`
    DROP INDEX `uk_reservation_slot`;

-- 중복 예약 판별 코드가 제약 이름 uk_reservation_slot을 쓰므로 이름을 유지한다.
ALTER TABLE `purpose_reservation`
    RENAME INDEX `uk_reservation_unit_slot` TO `uk_reservation_slot`;
