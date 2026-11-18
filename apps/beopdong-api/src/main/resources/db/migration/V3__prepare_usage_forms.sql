-- V2에서 보존한 진행 중 신청서를 같은 ID의 원본 이용에 연결한다.
UPDATE purpose_log history
JOIN purpose_usage usage_record ON usage_record.id = history.id
LEFT JOIN purpose_log linked ON linked.purpose_usage_id = usage_record.id
SET history.purpose_usage_id = usage_record.id
WHERE history.purpose_state = 'PENDING'
  AND history.purpose_usage_id IS NULL
  AND history.log_id = (SELECT member.log_id FROM purpose_usage_member member
                       WHERE member.purpose_usage_id = usage_record.id ORDER BY member.id LIMIT 1)
  AND linked.id IS NULL;

-- 신청서 없이 저장된 기존 이용만 준비한다. 기존 신청서의 이름과 시간은 보존한다.
SET SESSION group_concat_max_len = 8192;
INSERT INTO purpose_log (log_id, purpose_id, purpose_usage_id, purpose_state, name, seat, use_purpose, `count`, start_at, end_at)
SELECT first_member.log_id, usage_record.purpose_id, usage_record.id, 'PENDING', participants.names,
       usage_record.seat, usage_record.use_purpose,
       CASE WHEN usage_record.seat IS NULL THEN usage_record.`count` ELSE NULL END,
       usage_record.start_at, usage_record.end_at
FROM purpose_usage usage_record
JOIN (
    SELECT member.purpose_usage_id, MIN(member.id) AS first_id,
           GROUP_CONCAT(visitor.name ORDER BY member.joined_at, member.id SEPARATOR ', ') AS names
    FROM purpose_usage_member member JOIN `log` visitor ON visitor.id = member.log_id
    GROUP BY member.purpose_usage_id
) participants ON participants.purpose_usage_id = usage_record.id
JOIN purpose_usage_member first_member ON first_member.id = participants.first_id
WHERE usage_record.completed = FALSE
  AND NOT EXISTS (SELECT 1 FROM purpose_log history WHERE history.purpose_usage_id = usage_record.id);
