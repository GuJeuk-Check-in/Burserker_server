CREATE TABLE IF NOT EXISTS `purpose_reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `purpose_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `reservation_date` DATE NOT NULL,
    `start_time` VARCHAR(20) NOT NULL,
    `representative_phone` VARCHAR(20) NOT NULL,
    `male_count` INT NOT NULL,
    `female_count` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_reservation_slot` UNIQUE (`purpose_id`, `reservation_date`, `start_time`),
    INDEX `idx_reservation_user` (`user_id`),
    CONSTRAINT `fk_reservation_purpose` FOREIGN KEY (`purpose_id`) REFERENCES `purpose` (`id`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
