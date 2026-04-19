USE meridian_health;
INSERT INTO meridian_rule_config (id,rule_code,rule_name,rule_content_json,enabled,created_at,updated_at)
VALUES (10001,'DEFAULT_THRESHOLD_RULE','默认阈值规则','{"hrLow":50,"hrHigh":105,"spo2Low":94,"stressHigh":70,"fatigueHigh":75,"recoveryLow":35,"criticalHrLow":40,"criticalHrHigh":130,"criticalSpo2Low":90}',1,NOW(),NOW())
ON DUPLICATE KEY UPDATE rule_content_json=VALUES(rule_content_json),enabled=1,updated_at=NOW();

INSERT INTO user_profile (id,user_id,age,gender,height_cm,weight_kg,timezone,created_at,updated_at)
VALUES (20001,'U1001',30,'MALE',175,68.5,'Asia/Shanghai',NOW(),NOW())
ON DUPLICATE KEY UPDATE updated_at=NOW();
