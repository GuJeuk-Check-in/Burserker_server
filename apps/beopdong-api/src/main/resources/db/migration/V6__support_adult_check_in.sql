-- 성인 직접 방문은 개인정보 없이 이용대장 선택과 방문 시각만 저장한다.
ALTER TABLE `log`
    MODIFY COLUMN gender ENUM('MAN', 'WOMAN') NULL,
    MODIFY COLUMN age INT NULL;
