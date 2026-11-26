CREATE TABLE IF NOT EXISTS `sub_purpose` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `purpose_id` BIGINT NOT NULL,
    `sub_purpose` VARCHAR(30) NOT NULL,
    `sub_purpose_index` INT NOT NULL,
    `reservation_status` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_sub_purpose_name` UNIQUE (`purpose_id`, `sub_purpose`),
    CONSTRAINT `fk_sub_purpose_purpose` FOREIGN KEY (`purpose_id`) REFERENCES `purpose` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
