-- 법동 기본 스키마. 기존 DB는 구조 확인 및 백업 후 version 1로 baseline한다.
CREATE TABLE organ (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organ_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_organ_name UNIQUE (organ_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    gender ENUM('MAN', 'WOMAN') NOT NULL,
    phone VARCHAR(255) NOT NULL,
    birthymd DATE NOT NULL,
    grade INT NOT NULL,
    school VARCHAR(255) NOT NULL,
    route ENUM('FRIEND', 'PARENT', 'OTHER') NOT NULL,
    route_detail VARCHAR(255),
    address VARCHAR(255) NOT NULL,
    privacy_agreed BIT(1) NOT NULL,
    visit_count INT NOT NULL,
    apply_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `log` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    gender ENUM('MAN', 'WOMAN') NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(255) NOT NULL,
    visit_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purpose (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purpose_name VARCHAR(30) NOT NULL,
    purpose_index INT NOT NULL,
    organ_id BIGINT NOT NULL,
    locked BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purpose_organ FOREIGN KEY (organ_id) REFERENCES organ (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purpose_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purpose_state ENUM('NONE', 'PENDING', 'SUCCESS') NOT NULL,
    log_id BIGINT NOT NULL,
    purpose_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purpose_log_log FOREIGN KEY (log_id) REFERENCES `log` (id),
    CONSTRAINT fk_purpose_log_purpose FOREIGN KEY (purpose_id) REFERENCES purpose (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
