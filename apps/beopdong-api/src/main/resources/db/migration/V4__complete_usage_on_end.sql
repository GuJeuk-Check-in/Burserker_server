-- 참가자 Entity 통합 후에는 합류 시각을 직접 저장하지 않는다. 기존 값은 보존한다.
ALTER TABLE purpose_usage_member MODIFY COLUMN joined_at DATETIME(6) NULL;

SET @beopdong_completion_now = CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00');

-- 기존 완료 이력은 보존하고, 시간이 지난 PENDING 신청서는 같은 ID로 완료한다.
UPDATE purpose_log history
JOIN purpose_usage usage_record ON usage_record.id = history.purpose_usage_id
SET history.purpose_state = 'SUCCESS', usage_record.completed = TRUE
WHERE history.purpose_state = 'SUCCESS'
   OR (history.purpose_state = 'PENDING'
       AND (usage_record.completed = TRUE OR usage_record.end_at <= @beopdong_completion_now));

-- 진행 중 초안은 원본 행을 보존하되 연결을 해제한다.
-- 이후 실제 종료 시 새 SUCCESS 이력을 연결할 수 있도록 회차당 한 건의 자리를 비운다.
UPDATE purpose_log history
JOIN purpose_usage usage_record ON usage_record.id = history.purpose_usage_id
SET history.purpose_usage_id = NULL
WHERE history.purpose_state = 'PENDING'
  AND usage_record.completed = FALSE;
