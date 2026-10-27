ALTER TABLE `purpose_reservation`
    ADD COLUMN `name` VARCHAR(30) NULL AFTER `user_id`,
    MODIFY COLUMN `representative_phone` VARCHAR(20) NULL;

UPDATE `purpose_reservation` reservation
JOIN `user` member ON member.id = reservation.user_id
SET reservation.name = member.name
WHERE reservation.name IS NULL;

ALTER TABLE `purpose_reservation`
    MODIFY COLUMN `name` VARCHAR(30) NOT NULL;
