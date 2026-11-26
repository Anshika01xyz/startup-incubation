USE startup_platform;

-- Read-only verification. Run after database/sample_data.sql.
SELECT COUNT(*) AS live_startups FROM startups WHERE source_type='DEMO';
SELECT COUNT(*) AS live_applications FROM applications a JOIN startups s ON s.id=a.startup_id WHERE s.source_type='DEMO';
SELECT COUNT(*) AS live_funding_requests FROM funding_requests fr JOIN startups s ON s.id=fr.startup_id WHERE s.source_type='DEMO';
SELECT COUNT(*) AS live_investments FROM investments i JOIN funding_requests fr ON fr.id=i.funding_request_id JOIN startups s ON s.id=fr.startup_id WHERE s.source_type='DEMO';
SELECT COUNT(*) AS live_programmes FROM programs p JOIN incubators i ON i.id=p.incubator_id WHERE i.name='Northstar Innovation Hub';
SELECT COUNT(*) AS mentors_awaiting_approval FROM mentors WHERE approval_status='PENDING';
SELECT COUNT(*) AS live_startups_missing_application FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM applications a WHERE a.startup_id=s.id);
SELECT COUNT(*) AS live_startups_missing_funding_request FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM funding_requests fr WHERE fr.startup_id=s.id);
SELECT COUNT(*) AS live_startups_missing_investment FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM funding_requests fr JOIN investments i ON i.funding_request_id=fr.id WHERE fr.startup_id=s.id);
SELECT COUNT(*) AS live_startups_missing_team FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM startup_teams st WHERE st.startup_id=s.id);
SELECT COUNT(*) AS live_startups_missing_approved_mentor FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM mentor_assignments ma JOIN mentors m ON m.id=ma.mentor_id WHERE ma.startup_id=s.id AND m.approval_status='APPROVED');
SELECT COUNT(*) AS live_startups_missing_milestone FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM milestones ms WHERE ms.startup_id=s.id);
SELECT COUNT(*) AS live_startups_missing_progress FROM startups s WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM startup_progress sp WHERE sp.startup_id=s.id);
SELECT COUNT(*) AS applications_missing_evaluation FROM applications a JOIN startups s ON s.id=a.startup_id WHERE s.source_type='DEMO' AND NOT EXISTS (SELECT 1 FROM evaluations e WHERE e.application_id=a.id);
SELECT COUNT(*) AS imported_funding_rows FROM external_funding_rounds;
