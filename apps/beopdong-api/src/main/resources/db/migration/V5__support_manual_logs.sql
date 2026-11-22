-- 비회원 수기 방명록을 허용하고, 삭제 시 시설 기록과의 연결은 보존한다.
ALTER TABLE `log`
    MODIFY COLUMN user_id BIGINT NULL,
    MODIFY COLUMN name VARCHAR(255) NULL,
    MODIFY COLUMN phone VARCHAR(255) NULL,
    ADD COLUMN visit_purpose TEXT NULL,
    ADD COLUMN note TEXT NULL,
    ADD COLUMN deleted BIT(1) NOT NULL DEFAULT b'0';
