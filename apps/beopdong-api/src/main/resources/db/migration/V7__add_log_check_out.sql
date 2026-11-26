-- 수동 퇴실 시각을 보존한다. 3시간 자동 퇴실은 visit_at을 기준으로 판단한다.
ALTER TABLE `log` ADD COLUMN checked_out_at DATETIME(6) NULL;
