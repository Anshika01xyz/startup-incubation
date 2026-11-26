USE startup_platform;
-- Platform dashboard
SELECT 'Startups' metric, COUNT(*) total FROM startups
UNION ALL SELECT 'Incubation applications', COUNT(*) FROM applications
UNION ALL SELECT 'Mentors awaiting approval', COUNT(*) FROM mentors WHERE approval_status='PENDING'
UNION ALL SELECT 'Open funding requests', COUNT(*) FROM funding_requests WHERE status='OPEN'
UNION ALL SELECT 'Investment commitments', COUNT(*) FROM investments WHERE status='COMMITTED';
-- Startup 360-degree profile
SELECT * FROM v_startup_360 ORDER BY name;
-- Milestone health
SELECT * FROM v_milestone_health ORDER BY due_date;
-- Activity timeline
SELECT al.created_at,u.full_name,al.action,al.entity_type,al.entity_id,al.details
FROM activity_logs al LEFT JOIN users u ON u.id=al.actor_user_id ORDER BY al.created_at DESC LIMIT 100;
-- Funding history imported from public dataset
SELECT startup_name,country,city,sector,announced_date,funding_stage,amount_usd,lead_investor,investor_count,news_mentions,source_url
FROM external_funding_rounds ORDER BY startup_name;
