USE startup_platform;

-- DEMONSTRATION DATA ONLY. All operational companies/people below are fictional.
-- Demo password for all seeded accounts: Demo@12345 (PBKDF2-HMAC-SHA256, 210,000 rounds).
-- Change/remove these accounts before using any non-demo deployment.

START TRANSACTION;

-- Normalize earlier seed labels while keeping the existing operational records.
UPDATE users SET full_name='Foundry Administrator' WHERE email='admin.demo@foundry.local';
UPDATE funding_requests SET purpose='Product development and market expansion' WHERE purpose='Demo expansion and product development';
UPDATE milestones SET title=CASE title
    WHEN 'Demo milestone 1' THEN 'Complete supplier pilot onboarding'
    WHEN 'Demo milestone 2' THEN 'Validate customer onboarding journey'
    WHEN 'Demo milestone 3' THEN 'Launch first market pilot'
    WHEN 'Demo milestone 4' THEN 'Complete pricing validation'
    WHEN 'Demo milestone 5' THEN 'Release partner dashboard'
    WHEN 'Demo milestone 6' THEN 'Expand distribution partnerships'
    WHEN 'Demo milestone 7' THEN 'Complete product security review'
    WHEN 'Demo milestone 8' THEN 'Launch regional pilot'
    WHEN 'Demo milestone 9' THEN 'Validate retention metrics'
    WHEN 'Demo milestone 10' THEN 'Complete merchant onboarding'
    WHEN 'Demo milestone 11' THEN 'Launch customer support workflow'
    WHEN 'Demo milestone 12' THEN 'Complete platform integration'
    WHEN 'Demo milestone 13' THEN 'Validate unit economics'
    WHEN 'Demo milestone 14' THEN 'Expand channel partnerships'
    WHEN 'Demo milestone 15' THEN 'Complete compliance assessment'
    WHEN 'Demo milestone 16' THEN 'Release analytics dashboard'
    WHEN 'Demo milestone 17' THEN 'Launch mobile experience'
    WHEN 'Demo milestone 18' THEN 'Complete field testing'
    WHEN 'Demo milestone 19' THEN 'Validate customer acquisition'
    WHEN 'Demo milestone 20' THEN 'Prepare next funding milestone'
    ELSE title END WHERE title IN ('Demo milestone 1','Demo milestone 2','Demo milestone 3','Demo milestone 4','Demo milestone 5','Demo milestone 6','Demo milestone 7','Demo milestone 8','Demo milestone 9','Demo milestone 10','Demo milestone 11','Demo milestone 12','Demo milestone 13','Demo milestone 14','Demo milestone 15','Demo milestone 16','Demo milestone 17','Demo milestone 18','Demo milestone 19','Demo milestone 20');
UPDATE evaluations SET comments='Evaluation prepared for programme review.' WHERE comments='Demo evaluation record.';
UPDATE startup_progress SET summary='Progress update recorded for the shared workspace.' WHERE summary='Demo progress updated for shared role workspace.';
UPDATE founders SET bio='Founder profile for the shared Foundry workspace.' WHERE bio='Demo founder profile for the shared Foundry workspace.';
UPDATE activity_logs SET action='WORKSPACE_INITIALIZED',details='Operational workspace records initialized.' WHERE action='DEMO_DATA_LOADED';

INSERT INTO users(full_name,email,password_hash,status) VALUES
('Foundry Administrator','admin.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE'),
('Aarav Mehta','founder.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE'),
('Riya Kapoor','evaluator.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE'),
('Kabir Shah','mentor.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE'),
('Mira Ventures','investor.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE'),
('Northstar Programme Manager','incubator.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

SET @admin_id=(SELECT id FROM users WHERE email='admin.demo@foundry.local');
SET @founder_id=(SELECT id FROM users WHERE email='founder.demo@foundry.local');
SET @evaluator_id=(SELECT id FROM users WHERE email='evaluator.demo@foundry.local');
SET @mentor_user_id=(SELECT id FROM users WHERE email='mentor.demo@foundry.local');
SET @investor_user_id=(SELECT id FROM users WHERE email='investor.demo@foundry.local');
SET @incubator_user_id=(SELECT id FROM users WHERE email='incubator.demo@foundry.local');

INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @admin_id,id FROM roles WHERE role_name='ADMIN';
INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @founder_id,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @evaluator_id,id FROM roles WHERE role_name='EVALUATOR';
INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @mentor_user_id,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @investor_user_id,id FROM roles WHERE role_name='INVESTOR';
INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT @incubator_user_id,id FROM roles WHERE role_name='INCUBATOR';

INSERT IGNORE INTO founders(user_id,bio) VALUES(@founder_id,'Founder building sustainable logistics technology.');
INSERT IGNORE INTO evaluators(user_id,expertise) VALUES(@evaluator_id,'Market validation and product feasibility');
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status)
VALUES(@mentor_user_id,'Product strategy and go-to-market',9,'APPROVED');
UPDATE users SET status='ACTIVE' WHERE id=@mentor_user_id;
INSERT IGNORE INTO investors(user_id,organization,ticket_min,ticket_max)
VALUES(@investor_user_id,'Mira Ventures',500000,5000000);

INSERT INTO incubators(name,city,description,user_id)
VALUES('Northstar Innovation Hub','Chandigarh','Demo incubation partner for early-stage ventures.',@incubator_user_id)
ON DUPLICATE KEY UPDATE city=VALUES(city), user_id=COALESCE(user_id,VALUES(user_id));
SET @incubator_id=(SELECT id FROM incubators WHERE name='Northstar Innovation Hub');
INSERT INTO programs(incubator_id,name,duration_months)
SELECT @incubator_id,'Launchpad Cohort',6
WHERE NOT EXISTS(SELECT 1 FROM programs WHERE incubator_id=@incubator_id AND name='Launchpad Cohort');
SET @program_id=(SELECT id FROM programs WHERE incubator_id=@incubator_id AND name='Launchpad Cohort');

INSERT INTO programs(incubator_id,name,duration_months)
SELECT @incubator_id,'Sustainability Scale-Up',9
WHERE NOT EXISTS(SELECT 1 FROM programs WHERE incubator_id=@incubator_id AND name='Sustainability Scale-Up');
INSERT INTO programs(incubator_id,name,duration_months)
SELECT @incubator_id,'Digital India Accelerator',6
WHERE NOT EXISTS(SELECT 1 FROM programs WHERE incubator_id=@incubator_id AND name='Digital India Accelerator');
INSERT INTO programs(incubator_id,name,duration_months)
SELECT @incubator_id,'Market Access Cohort',8
WHERE NOT EXISTS(SELECT 1 FROM programs WHERE incubator_id=@incubator_id AND name='Market Access Cohort');
INSERT INTO programs(incubator_id,name,duration_months)
SELECT @incubator_id,'Venture Launch Programme',12
WHERE NOT EXISTS(SELECT 1 FROM programs WHERE incubator_id=@incubator_id AND name='Venture Launch Programme');
SET @program_2=(SELECT id FROM programs WHERE incubator_id=@incubator_id AND name='Sustainability Scale-Up');
SET @program_3=(SELECT id FROM programs WHERE incubator_id=@incubator_id AND name='Digital India Accelerator');
SET @program_4=(SELECT id FROM programs WHERE incubator_id=@incubator_id AND name='Market Access Cohort');
SET @program_5=(SELECT id FROM programs WHERE incubator_id=@incubator_id AND name='Venture Launch Programme');

INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type)
SELECT 'GreenRoute Mobility','ClimateTech','Pune','Fleet optimization software that reduces fuel use and delivery emissions.',2022,'Seed',@founder_id,'INCUBATED','DEMO'
WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='GreenRoute Mobility' AND founder_user_id=@founder_id);
SET @startup_id=(SELECT id FROM startups WHERE name='GreenRoute Mobility' AND founder_user_id=@founder_id ORDER BY id DESC LIMIT 1);

INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role)
SELECT @startup_id,id,'Founder' FROM founders WHERE user_id=@founder_id;
INSERT INTO startup_teams(startup_id,member_name,member_role,email,joined_on)
SELECT @startup_id,'Nisha Rao','Technology Lead','nisha.demo@example.test',CURDATE()
WHERE NOT EXISTS(SELECT 1 FROM startup_teams WHERE startup_id=@startup_id AND member_name='Nisha Rao');

INSERT INTO applications(startup_id,program_id,application_date,status)
SELECT @startup_id,@program_id,CURDATE(),'ACCEPTED'
WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@startup_id AND program_id=@program_id);
SET @application_id=(SELECT id FROM applications WHERE startup_id=@startup_id AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation)
SELECT @application_id,@evaluator_id,8.5,8,7.5,9,'Strong founding team and a clear sustainability use case.','ACCEPT'
WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@application_id);
SET @evaluation_id=(SELECT id FROM evaluations WHERE application_id=@application_id);
INSERT IGNORE INTO evaluation_scores(evaluation_id,criterion_id,score)
SELECT @evaluation_id,id,CASE criterion_name
 WHEN 'Innovation' THEN 8.5 WHEN 'Market' THEN 8 WHEN 'Feasibility' THEN 7.5 ELSE 9 END
FROM evaluation_criteria;

INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by)
SELECT m.id,@startup_id,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status)
SELECT @startup_id,'Complete first logistics pilot',DATE_ADD(CURDATE(),INTERVAL 14 DAY),'IN_PROGRESS'
WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@startup_id AND title='Complete first logistics pilot');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by)
SELECT @startup_id,45,'Pilot onboarding and route analytics are in progress.',@founder_id
WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@startup_id AND summary='Pilot onboarding and route analytics are in progress.');

INSERT IGNORE INTO investor_interests(investor_id,sector)
SELECT id,'ClimateTech' FROM investors WHERE user_id=@investor_user_id;
INSERT IGNORE INTO investor_stage_preferences(investor_id,preferred_stage)
SELECT id,'Seed' FROM investors WHERE user_id=@investor_user_id;

INSERT INTO funding_requests(startup_id,amount,purpose,currency,status)
SELECT @startup_id,2500000,'Expand the commercial pilot to two additional cities','INR','OPEN'
WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@startup_id AND purpose='Expand the commercial pilot to two additional cities');
SET @funding_id=(SELECT id FROM funding_requests WHERE startup_id=@startup_id AND purpose='Expand the commercial pilot to two additional cities');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status)
SELECT @funding_id,id,750000,'INR','COMMITTED' FROM investors WHERE user_id=@investor_user_id
AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@funding_id AND investor_id=(SELECT id FROM investors WHERE user_id=@investor_user_id));

INSERT INTO activity_logs(actor_user_id,action,entity_type,entity_id,details)
SELECT @admin_id,'WORKSPACE_INITIALIZED','PLATFORM',1,'Operational workspace records initialized.'
WHERE NOT EXISTS(SELECT 1 FROM activity_logs WHERE action='WORKSPACE_INITIALIZED' AND details='Operational workspace records initialized.');


-- Expanded shared role-based live demo ecosystem. Existing application code and historical CSV are unchanged.
-- Demo users use the same documented demo password hash as the original accounts.
INSERT INTO users(full_name,email,password_hash,status) VALUES('Ananya Rao','founder01.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f1=(SELECT id FROM users WHERE email='founder01.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f1,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f1,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Vihaan Sharma','founder02.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f2=(SELECT id FROM users WHERE email='founder02.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f2,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f2,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Ishita Nair','founder03.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f3=(SELECT id FROM users WHERE email='founder03.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f3,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f3,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Arjun Patel','founder04.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f4=(SELECT id FROM users WHERE email='founder04.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f4,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f4,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Meera Iyer','founder05.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f5=(SELECT id FROM users WHERE email='founder05.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f5,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f5,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Kabir Malhotra','founder06.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f6=(SELECT id FROM users WHERE email='founder06.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f6,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f6,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Sana Khan','founder07.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f7=(SELECT id FROM users WHERE email='founder07.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f7,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f7,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Dev Kapoor','founder08.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f8=(SELECT id FROM users WHERE email='founder08.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f8,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f8,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Aditi Menon','founder09.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f9=(SELECT id FROM users WHERE email='founder09.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f9,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f9,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Rohan Das','founder10.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f10=(SELECT id FROM users WHERE email='founder10.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f10,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f10,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Tara Joshi','founder11.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f11=(SELECT id FROM users WHERE email='founder11.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f11,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f11,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Neil Fernandes','founder12.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f12=(SELECT id FROM users WHERE email='founder12.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f12,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f12,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Kavya Singh','founder13.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f13=(SELECT id FROM users WHERE email='founder13.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f13,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f13,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Yash Bansal','founder14.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f14=(SELECT id FROM users WHERE email='founder14.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f14,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f14,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Diya Kulkarni','founder15.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f15=(SELECT id FROM users WHERE email='founder15.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f15,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f15,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Rehan Ali','founder16.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f16=(SELECT id FROM users WHERE email='founder16.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f16,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f16,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mira Sethi','founder17.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f17=(SELECT id FROM users WHERE email='founder17.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f17,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f17,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Aarush Jain','founder18.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f18=(SELECT id FROM users WHERE email='founder18.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f18,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f18,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Naina Bose','founder19.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f19=(SELECT id FROM users WHERE email='founder19.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f19,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f19,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Om Prakash','founder20.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @f20=(SELECT id FROM users WHERE email='founder20.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @f20,id FROM roles WHERE role_name='FOUNDER';
INSERT IGNORE INTO founders(user_id,bio) VALUES(@f20,'Founder profile for the shared Foundry workspace.');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Asha Verma','mentor02.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @mentor02=(SELECT id FROM users WHERE email='mentor02.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor02,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor02,'Startup growth and operating strategy',7,'APPROVED');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Rahul Sen','mentor03.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @mentor03=(SELECT id FROM users WHERE email='mentor03.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor03,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor03,'Startup growth and operating strategy',7,'APPROVED');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Pooja Rao','mentor04.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @mentor04=(SELECT id FROM users WHERE email='mentor04.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor04,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor04,'Startup growth and operating strategy',7,'APPROVED');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Imran Sheikh','mentor05.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @mentor05=(SELECT id FROM users WHERE email='mentor05.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor05,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor05,'Startup growth and operating strategy',7,'APPROVED');
SET @mentor01=@mentor_user_id;

-- Additional mentor applicants populate the approval queue; approval remains an admin action.
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Tara Iyer','mentor06.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','PENDING') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);
SET @mentor06=(SELECT id FROM users WHERE email='mentor06.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor06,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor06,'Climate technology and sustainability',8,'PENDING');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Dev Malhotra','mentor07.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','PENDING') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);
SET @mentor07=(SELECT id FROM users WHERE email='mentor07.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor07,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor07,'FinTech and financial operations',6,'PENDING');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Mentor Sana Qureshi','mentor08.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','PENDING') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);
SET @mentor08=(SELECT id FROM users WHERE email='mentor08.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @mentor08,id FROM roles WHERE role_name='MENTOR';
INSERT IGNORE INTO mentors(user_id,expertise,experience_years,approval_status) VALUES(@mentor08,'Healthcare and digital product strategy',10,'PENDING');

INSERT INTO users(full_name,email,password_hash,status) VALUES('Cedar Seed Fund','investor02.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @investor02=(SELECT id FROM users WHERE email='investor02.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @investor02,id FROM roles WHERE role_name='INVESTOR';
INSERT IGNORE INTO investors(user_id,organization,ticket_min,ticket_max) VALUES(@investor02,'Cedar Seed Fund',250000,7500000);
INSERT INTO users(full_name,email,password_hash,status) VALUES('Pioneer Capital','investor03.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @investor03=(SELECT id FROM users WHERE email='investor03.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @investor03,id FROM roles WHERE role_name='INVESTOR';
INSERT IGNORE INTO investors(user_id,organization,ticket_min,ticket_max) VALUES(@investor03,'Pioneer Capital',250000,7500000);
INSERT INTO users(full_name,email,password_hash,status) VALUES('BluePeak Ventures','investor04.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @investor04=(SELECT id FROM users WHERE email='investor04.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @investor04,id FROM roles WHERE role_name='INVESTOR';
INSERT IGNORE INTO investors(user_id,organization,ticket_min,ticket_max) VALUES(@investor04,'BluePeak Ventures',250000,7500000);
INSERT INTO users(full_name,email,password_hash,status) VALUES('Saffron Angels','investor05.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @investor05=(SELECT id FROM users WHERE email='investor05.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @investor05,id FROM roles WHERE role_name='INVESTOR';
INSERT IGNORE INTO investors(user_id,organization,ticket_min,ticket_max) VALUES(@investor05,'Saffron Angels',250000,7500000);
INSERT INTO users(full_name,email,password_hash,status) VALUES('Evaluator Nikhil Shah','evaluator02.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @evaluator02=(SELECT id FROM users WHERE email='evaluator02.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @evaluator02,id FROM roles WHERE role_name='EVALUATOR';
INSERT IGNORE INTO evaluators(user_id,expertise) VALUES(@evaluator02,'Startup evaluation and market readiness');
INSERT INTO users(full_name,email,password_hash,status) VALUES('Evaluator Leena Roy','evaluator03.demo@foundry.local','pbkdf2$210000$ABEiM0RVZneImaq7zN3u/w==$Z+RrPhgLXLl8fEiPUABXlztUHuwh+sO0gKttKJbMbpI=','ACTIVE') ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),status='ACTIVE';
SET @evaluator03=(SELECT id FROM users WHERE email='evaluator03.demo@foundry.local');
INSERT IGNORE INTO user_roles(user_id,role_id) SELECT @evaluator03,id FROM roles WHERE role_name='EVALUATOR';
INSERT IGNORE INTO evaluators(user_id,expertise) VALUES(@evaluator03,'Startup evaluation and market readiness');
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'SolarSutra','Clean Energy','Jaipur','Rooftop solar financing and monitoring for small businesses.',2021,'Seed',@f1,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='SolarSutra' AND founder_user_id=@f1);
SET @s1=(SELECT id FROM startups WHERE name='SolarSutra' AND founder_user_id=@f1 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s1,id,'Founder' FROM founders WHERE user_id=@f1;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s1,@program_id,DATE_SUB(CURDATE(),INTERVAL 1 DAY),'UNDER_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s1 AND program_id=@program_id);
SET @a1=(SELECT id FROM applications WHERE startup_id=@s1 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a1,@evaluator_id, 7.0, 7.5, 6.5, 7.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a1);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s1,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s1,'Complete supplier pilot onboarding',DATE_ADD(CURDATE(),INTERVAL 3 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s1 AND title='Complete supplier pilot onboarding');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s1,27,'Progress update recorded for the shared workspace.',@f1 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s1 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s1,625000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s1 AND purpose='Product development and market expansion');
SET @r1=(SELECT id FROM funding_requests WHERE startup_id=@s1 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r1,iv.id,125000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor02 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r1 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'AgriPulse','AgriTech','Indore','Precision agriculture tools for smallholder farms.',2022,'Pre-Seed',@f2,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='AgriPulse' AND founder_user_id=@f2);
SET @s2=(SELECT id FROM startups WHERE name='AgriPulse' AND founder_user_id=@f2 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s2,id,'Founder' FROM founders WHERE user_id=@f2;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s2,@program_id,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'SUBMITTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s2 AND program_id=@program_id);
SET @a2=(SELECT id FROM applications WHERE startup_id=@s2 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a2,@evaluator_id, 7.5, 8.0, 7.0, 8.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a2);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s2,@admin_id FROM mentors m WHERE m.user_id=@mentor03;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s2,'Validate customer onboarding journey',DATE_ADD(CURDATE(),INTERVAL 6 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s2 AND title='Validate customer onboarding journey');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s2,34,'Progress update recorded for the shared workspace.',@f2 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s2 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s2,750000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s2 AND purpose='Product development and market expansion');
SET @r2=(SELECT id FROM funding_requests WHERE startup_id=@s2 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r2,iv.id,150000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor03 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r2 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'MedBridge','HealthTech','Hyderabad','Digital care coordination for neighborhood clinics.',2020,'Seed',@f3,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='MedBridge' AND founder_user_id=@f3);
SET @s3=(SELECT id FROM startups WHERE name='MedBridge' AND founder_user_id=@f3 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s3,id,'Founder' FROM founders WHERE user_id=@f3;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s3,@program_id,DATE_SUB(CURDATE(),INTERVAL 3 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s3 AND program_id=@program_id);
SET @a3=(SELECT id FROM applications WHERE startup_id=@s3 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a3,@evaluator_id, 8.0, 7.0, 7.5, 8.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a3);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s3,@admin_id FROM mentors m WHERE m.user_id=@mentor04;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s3,'Launch first market pilot',DATE_ADD(CURDATE(),INTERVAL 9 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s3 AND title='Launch first market pilot');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s3,41,'Progress update recorded for the shared workspace.',@f3 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s3 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s3,875000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s3 AND purpose='Product development and market expansion');
SET @r3=(SELECT id FROM funding_requests WHERE startup_id=@s3 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r3,iv.id,175000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor04 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r3 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'SkillSpring','EdTech','Lucknow','Career-linked learning for emerging professionals.',2023,'Pre-Seed',@f4,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='SkillSpring' AND founder_user_id=@f4);
SET @s4=(SELECT id FROM startups WHERE name='SkillSpring' AND founder_user_id=@f4 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s4,id,'Founder' FROM founders WHERE user_id=@f4;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s4,@program_id,DATE_SUB(CURDATE(),INTERVAL 4 DAY),'REJECTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s4 AND program_id=@program_id);
SET @a4=(SELECT id FROM applications WHERE startup_id=@s4 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a4,@evaluator_id, 6.5, 7.5, 8.0, 7.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a4);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s4,@admin_id FROM mentors m WHERE m.user_id=@mentor01;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s4,'Complete pricing validation',DATE_ADD(CURDATE(),INTERVAL 12 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s4 AND title='Complete pricing validation');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s4,48,'Progress update recorded for the shared workspace.',@f4 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s4 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s4,1000000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s4 AND purpose='Product development and market expansion');
SET @r4=(SELECT id FROM funding_requests WHERE startup_id=@s4 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r4,iv.id,200000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor05 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r4 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'BlueCart','Retail','Mumbai','Inventory and commerce tools for local retailers.',2021,'Seed',@f5,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='BlueCart' AND founder_user_id=@f5);
SET @s5=(SELECT id FROM startups WHERE name='BlueCart' AND founder_user_id=@f5 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s5,id,'Founder' FROM founders WHERE user_id=@f5;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s5,@program_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s5 AND program_id=@program_id);
SET @a5=(SELECT id FROM applications WHERE startup_id=@s5 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a5,@evaluator_id, 7.0, 8.0, 6.0, 7.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a5);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s5,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s5,'Release partner dashboard',DATE_ADD(CURDATE(),INTERVAL 15 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s5 AND title='Release partner dashboard');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s5,55,'Progress update recorded for the shared workspace.',@f5 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s5 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s5,1125000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s5 AND purpose='Product development and market expansion');
SET @r5=(SELECT id FROM funding_requests WHERE startup_id=@s5 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r5,iv.id,225000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor_user_id AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r5 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'ReCircle','Circular Economy','Pune','Reusable packaging logistics for urban commerce.',2022,'Seed',@f6,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='ReCircle' AND founder_user_id=@f6);
SET @s6=(SELECT id FROM startups WHERE name='ReCircle' AND founder_user_id=@f6 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s6,id,'Founder' FROM founders WHERE user_id=@f6;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s6,@program_id,DATE_SUB(CURDATE(),INTERVAL 6 DAY),'UNDER_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s6 AND program_id=@program_id);
SET @a6=(SELECT id FROM applications WHERE startup_id=@s6 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a6,@evaluator_id, 7.5, 7.0, 6.5, 8.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a6);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s6,@admin_id FROM mentors m WHERE m.user_id=@mentor03;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s6,'Expand distribution partnerships',DATE_ADD(CURDATE(),INTERVAL 18 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s6 AND title='Expand distribution partnerships');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s6,62,'Progress update recorded for the shared workspace.',@f6 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s6 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s6,1250000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s6 AND purpose='Product development and market expansion');
SET @r6=(SELECT id FROM funding_requests WHERE startup_id=@s6 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r6,iv.id,250000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor02 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r6 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'NammaTransit','Mobility','Bengaluru','Route planning and fleet tools for shared mobility.',2020,'Seed',@f7,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='NammaTransit' AND founder_user_id=@f7);
SET @s7=(SELECT id FROM startups WHERE name='NammaTransit' AND founder_user_id=@f7 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s7,id,'Founder' FROM founders WHERE user_id=@f7;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s7,@program_id,DATE_SUB(CURDATE(),INTERVAL 7 DAY),'SUBMITTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s7 AND program_id=@program_id);
SET @a7=(SELECT id FROM applications WHERE startup_id=@s7 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a7,@evaluator_id, 8.0, 7.5, 7.0, 8.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a7);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s7,@admin_id FROM mentors m WHERE m.user_id=@mentor04;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s7,'Complete product security review',DATE_ADD(CURDATE(),INTERVAL 21 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s7 AND title='Complete product security review');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s7,69,'Progress update recorded for the shared workspace.',@f7 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s7 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s7,1375000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s7 AND purpose='Product development and market expansion');
SET @r7=(SELECT id FROM funding_requests WHERE startup_id=@s7 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r7,iv.id,275000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor03 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r7 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'AquaSage','WaterTech','Chennai','Water quality monitoring for communities and industry.',2023,'Pre-Seed',@f8,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='AquaSage' AND founder_user_id=@f8);
SET @s8=(SELECT id FROM startups WHERE name='AquaSage' AND founder_user_id=@f8 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s8,id,'Founder' FROM founders WHERE user_id=@f8;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s8,@program_id,DATE_SUB(CURDATE(),INTERVAL 8 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s8 AND program_id=@program_id);
SET @a8=(SELECT id FROM applications WHERE startup_id=@s8 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a8,@evaluator_id, 6.5, 8.0, 7.5, 7.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a8);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s8,@admin_id FROM mentors m WHERE m.user_id=@mentor01;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s8,'Launch regional pilot',DATE_ADD(CURDATE(),INTERVAL 24 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s8 AND title='Launch regional pilot');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s8,76,'Progress update recorded for the shared workspace.',@f8 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s8 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s8,1500000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s8 AND purpose='Product development and market expansion');
SET @r8=(SELECT id FROM funding_requests WHERE startup_id=@s8 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r8,iv.id,300000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor04 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r8 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'CraftLink','Handicrafts','Jaipur','Digital distribution for artisan-led brands.',2021,'Seed',@f9,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='CraftLink' AND founder_user_id=@f9);
SET @s9=(SELECT id FROM startups WHERE name='CraftLink' AND founder_user_id=@f9 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s9,id,'Founder' FROM founders WHERE user_id=@f9;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s9,@program_id,DATE_SUB(CURDATE(),INTERVAL 9 DAY),'REJECTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s9 AND program_id=@program_id);
SET @a9=(SELECT id FROM applications WHERE startup_id=@s9 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a9,@evaluator_id, 7.0, 7.0, 8.0, 7.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a9);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s9,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s9,'Validate retention metrics',DATE_ADD(CURDATE(),INTERVAL 27 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s9 AND title='Validate retention metrics');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s9,83,'Progress update recorded for the shared workspace.',@f9 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s9 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s9,1625000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s9 AND purpose='Product development and market expansion');
SET @r9=(SELECT id FROM funding_requests WHERE startup_id=@s9 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r9,iv.id,325000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor05 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r9 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'FarmGate','AgriTech','Nagpur','Market access and logistics for farm produce.',2022,'Pre-Seed',@f10,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='FarmGate' AND founder_user_id=@f10);
SET @s10=(SELECT id FROM startups WHERE name='FarmGate' AND founder_user_id=@f10 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s10,id,'Founder' FROM founders WHERE user_id=@f10;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s10,@program_id,DATE_SUB(CURDATE(),INTERVAL 10 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s10 AND program_id=@program_id);
SET @a10=(SELECT id FROM applications WHERE startup_id=@s10 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a10,@evaluator_id, 7.5, 7.5, 6.0, 8.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a10);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s10,@admin_id FROM mentors m WHERE m.user_id=@mentor03;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s10,'Complete merchant onboarding',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s10 AND title='Complete merchant onboarding');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s10,90,'Progress update recorded for the shared workspace.',@f10 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s10 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s10,1750000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s10 AND purpose='Product development and market expansion');
SET @r10=(SELECT id FROM funding_requests WHERE startup_id=@s10 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r10,iv.id,350000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor_user_id AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r10 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'CareNest','HealthTech','Kochi','At-home care coordination for older adults.',2021,'Seed',@f11,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='CareNest' AND founder_user_id=@f11);
SET @s11=(SELECT id FROM startups WHERE name='CareNest' AND founder_user_id=@f11 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s11,id,'Founder' FROM founders WHERE user_id=@f11;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s11,@program_id,DATE_SUB(CURDATE(),INTERVAL 11 DAY),'UNDER_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s11 AND program_id=@program_id);
SET @a11=(SELECT id FROM applications WHERE startup_id=@s11 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a11,@evaluator_id, 8.0, 8.0, 6.5, 8.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a11);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s11,@admin_id FROM mentors m WHERE m.user_id=@mentor04;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s11,'Launch customer support workflow',DATE_ADD(CURDATE(),INTERVAL 33 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s11 AND title='Launch customer support workflow');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s11,22,'Progress update recorded for the shared workspace.',@f11 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s11 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s11,1875000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s11 AND purpose='Product development and market expansion');
SET @r11=(SELECT id FROM funding_requests WHERE startup_id=@s11 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r11,iv.id,375000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor02 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r11 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'WasteWise','CleanTech','Ahmedabad','Waste collection and material recovery platform.',2020,'Seed',@f12,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='WasteWise' AND founder_user_id=@f12);
SET @s12=(SELECT id FROM startups WHERE name='WasteWise' AND founder_user_id=@f12 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s12,id,'Founder' FROM founders WHERE user_id=@f12;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s12,@program_id,DATE_SUB(CURDATE(),INTERVAL 12 DAY),'SUBMITTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s12 AND program_id=@program_id);
SET @a12=(SELECT id FROM applications WHERE startup_id=@s12 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a12,@evaluator_id, 6.5, 7.0, 7.0, 7.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a12);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s12,@admin_id FROM mentors m WHERE m.user_id=@mentor01;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s12,'Complete platform integration',DATE_ADD(CURDATE(),INTERVAL 36 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s12 AND title='Complete platform integration');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s12,29,'Progress update recorded for the shared workspace.',@f12 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s12 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s12,2000000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s12 AND purpose='Product development and market expansion');
SET @r12=(SELECT id FROM funding_requests WHERE startup_id=@s12 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r12,iv.id,400000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor03 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r12 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'ByteBhasha','AI','Noida','Indian-language AI tools for small businesses.',2023,'Pre-Seed',@f13,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='ByteBhasha' AND founder_user_id=@f13);
SET @s13=(SELECT id FROM startups WHERE name='ByteBhasha' AND founder_user_id=@f13 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s13,id,'Founder' FROM founders WHERE user_id=@f13;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s13,@program_id,DATE_SUB(CURDATE(),INTERVAL 13 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s13 AND program_id=@program_id);
SET @a13=(SELECT id FROM applications WHERE startup_id=@s13 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a13,@evaluator_id, 7.0, 7.5, 7.5, 7.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a13);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s13,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s13,'Validate unit economics',DATE_ADD(CURDATE(),INTERVAL 39 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s13 AND title='Validate unit economics');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s13,36,'Progress update recorded for the shared workspace.',@f13 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s13 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s13,2125000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s13 AND purpose='Product development and market expansion');
SET @r13=(SELECT id FROM funding_requests WHERE startup_id=@s13 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r13,iv.id,425000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor04 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r13 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'RuralStack','FinTech','Bhopal','Digital bookkeeping and cashflow tools for rural enterprises.',2022,'Seed',@f14,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='RuralStack' AND founder_user_id=@f14);
SET @s14=(SELECT id FROM startups WHERE name='RuralStack' AND founder_user_id=@f14 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s14,id,'Founder' FROM founders WHERE user_id=@f14;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s14,@program_id,DATE_SUB(CURDATE(),INTERVAL 14 DAY),'REJECTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s14 AND program_id=@program_id);
SET @a14=(SELECT id FROM applications WHERE startup_id=@s14 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a14,@evaluator_id, 7.5, 8.0, 8.0, 8.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a14);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s14,@admin_id FROM mentors m WHERE m.user_id=@mentor03;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s14,'Expand channel partnerships',DATE_ADD(CURDATE(),INTERVAL 42 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s14 AND title='Expand channel partnerships');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s14,43,'Progress update recorded for the shared workspace.',@f14 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s14 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s14,2250000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s14 AND purpose='Product development and market expansion');
SET @r14=(SELECT id FROM funding_requests WHERE startup_id=@s14 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r14,iv.id,450000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor05 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r14 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'UrbanHarvest','FoodTech','Gurugram','Local produce sourcing for restaurants and households.',2021,'Seed',@f15,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='UrbanHarvest' AND founder_user_id=@f15);
SET @s15=(SELECT id FROM startups WHERE name='UrbanHarvest' AND founder_user_id=@f15 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s15,id,'Founder' FROM founders WHERE user_id=@f15;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s15,@program_id,DATE_SUB(CURDATE(),INTERVAL 15 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s15 AND program_id=@program_id);
SET @a15=(SELECT id FROM applications WHERE startup_id=@s15 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a15,@evaluator_id, 8.0, 7.0, 6.0, 8.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a15);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s15,@admin_id FROM mentors m WHERE m.user_id=@mentor04;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s15,'Complete compliance assessment',DATE_ADD(CURDATE(),INTERVAL 45 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s15 AND title='Complete compliance assessment');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s15,50,'Progress update recorded for the shared workspace.',@f15 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s15 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s15,2375000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s15 AND purpose='Product development and market expansion');
SET @r15=(SELECT id FROM funding_requests WHERE startup_id=@s15 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r15,iv.id,475000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor_user_id AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r15 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'SafeSteps','SafetyTech','Delhi','Workplace safety reporting and compliance tools.',2022,'Pre-Seed',@f16,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='SafeSteps' AND founder_user_id=@f16);
SET @s16=(SELECT id FROM startups WHERE name='SafeSteps' AND founder_user_id=@f16 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s16,id,'Founder' FROM founders WHERE user_id=@f16;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s16,@program_id,DATE_SUB(CURDATE(),INTERVAL 16 DAY),'UNDER_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s16 AND program_id=@program_id);
SET @a16=(SELECT id FROM applications WHERE startup_id=@s16 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a16,@evaluator_id, 6.5, 7.5, 6.5, 7.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a16);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s16,@admin_id FROM mentors m WHERE m.user_id=@mentor01;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s16,'Release analytics dashboard',DATE_ADD(CURDATE(),INTERVAL 48 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s16 AND title='Release analytics dashboard');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s16,57,'Progress update recorded for the shared workspace.',@f16 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s16 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s16,2500000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s16 AND purpose='Product development and market expansion');
SET @r16=(SELECT id FROM funding_requests WHERE startup_id=@s16 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r16,iv.id,500000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor02 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r16 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'EduOrbit','EdTech','Coimbatore','School administration and parent communication software.',2020,'Seed',@f17,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='EduOrbit' AND founder_user_id=@f17);
SET @s17=(SELECT id FROM startups WHERE name='EduOrbit' AND founder_user_id=@f17 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s17,id,'Founder' FROM founders WHERE user_id=@f17;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s17,@program_id,DATE_SUB(CURDATE(),INTERVAL 17 DAY),'SUBMITTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s17 AND program_id=@program_id);
SET @a17=(SELECT id FROM applications WHERE startup_id=@s17 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a17,@evaluator_id, 7.0, 8.0, 7.0, 7.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a17);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s17,@admin_id FROM mentors m WHERE m.user_id=@mentor_user_id;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s17,'Launch mobile experience',DATE_ADD(CURDATE(),INTERVAL 51 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s17 AND title='Launch mobile experience');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s17,64,'Progress update recorded for the shared workspace.',@f17 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s17 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s17,2625000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s17 AND purpose='Product development and market expansion');
SET @r17=(SELECT id FROM funding_requests WHERE startup_id=@s17 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r17,iv.id,525000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor03 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r17 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'ChargeGrid','EVTech','Surat','EV charging discovery and operations platform.',2023,'Pre-Seed',@f18,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='ChargeGrid' AND founder_user_id=@f18);
SET @s18=(SELECT id FROM startups WHERE name='ChargeGrid' AND founder_user_id=@f18 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s18,id,'Founder' FROM founders WHERE user_id=@f18;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s18,@program_id,DATE_SUB(CURDATE(),INTERVAL 18 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s18 AND program_id=@program_id);
SET @a18=(SELECT id FROM applications WHERE startup_id=@s18 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a18,@evaluator_id, 7.5, 7.0, 7.5, 8.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a18);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s18,@admin_id FROM mentors m WHERE m.user_id=@mentor03;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s18,'Complete field testing',DATE_ADD(CURDATE(),INTERVAL 54 DAY),'COMPLETED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s18 AND title='Complete field testing');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s18,71,'Progress update recorded for the shared workspace.',@f18 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s18 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s18,2750000,'Product development and market expansion','INR','OPEN' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s18 AND purpose='Product development and market expansion');
SET @r18=(SELECT id FROM funding_requests WHERE startup_id=@s18 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r18,iv.id,550000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor04 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r18 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'SwasthyaNow','HealthTech','Patna','Appointment and follow-up tools for primary care.',2022,'Seed',@f19,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='SwasthyaNow' AND founder_user_id=@f19);
SET @s19=(SELECT id FROM startups WHERE name='SwasthyaNow' AND founder_user_id=@f19 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s19,id,'Founder' FROM founders WHERE user_id=@f19;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s19,@program_id,DATE_SUB(CURDATE(),INTERVAL 19 DAY),'REJECTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s19 AND program_id=@program_id);
SET @a19=(SELECT id FROM applications WHERE startup_id=@s19 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a19,@evaluator_id, 8.0, 7.5, 8.0, 8.5, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a19);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s19,@admin_id FROM mentors m WHERE m.user_id=@mentor04;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s19,'Validate customer acquisition',DATE_ADD(CURDATE(),INTERVAL 57 DAY),'IN_PROGRESS' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s19 AND title='Validate customer acquisition');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s19,78,'Progress update recorded for the shared workspace.',@f19 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s19 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s19,2875000,'Product development and market expansion','INR','IN_REVIEW' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s19 AND purpose='Product development and market expansion');
SET @r19=(SELECT id FROM funding_requests WHERE startup_id=@s19 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r19,iv.id,575000,'INR','RECEIVED' FROM investors iv WHERE iv.user_id=@investor05 AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r19 AND investor_id=iv.id);
INSERT INTO startups(name,sector,city,description,founded_year,stage,founder_user_id,status,source_type) SELECT 'LocalLedger','FinTech','Visakhapatnam','Simple financial operations for neighborhood merchants.',2021,'Seed',@f20,'INCUBATED','DEMO' WHERE NOT EXISTS(SELECT 1 FROM startups WHERE name='LocalLedger' AND founder_user_id=@f20);
SET @s20=(SELECT id FROM startups WHERE name='LocalLedger' AND founder_user_id=@f20 ORDER BY id DESC LIMIT 1);
INSERT IGNORE INTO startup_founders(startup_id,founder_id,founder_role) SELECT @s20,id,'Founder' FROM founders WHERE user_id=@f20;
INSERT INTO applications(startup_id,program_id,application_date,status) SELECT @s20,@program_id,DATE_SUB(CURDATE(),INTERVAL 20 DAY),'ACCEPTED' WHERE NOT EXISTS(SELECT 1 FROM applications WHERE startup_id=@s20 AND program_id=@program_id);
SET @a20=(SELECT id FROM applications WHERE startup_id=@s20 AND program_id=@program_id);
INSERT INTO evaluations(application_id,evaluator_user_id,innovation_score,market_score,feasibility_score,team_score,comments,recommendation) SELECT @a20,@evaluator_id, 6.5, 8.0, 6.0, 7.0, 'Evaluation prepared for programme review.', 'REVIEW' WHERE NOT EXISTS(SELECT 1 FROM evaluations WHERE application_id=@a20);
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by) SELECT m.id,@s20,@admin_id FROM mentors m WHERE m.user_id=@mentor01;
INSERT INTO milestones(startup_id,title,due_date,status) SELECT @s20,'Prepare next funding milestone',DATE_ADD(CURDATE(),INTERVAL 60 DAY),'NOT_STARTED' WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@s20 AND title='Prepare next funding milestone');
INSERT INTO startup_progress(startup_id,progress_percent,summary,updated_by) SELECT @s20,85,'Progress update recorded for the shared workspace.',@f20 WHERE NOT EXISTS(SELECT 1 FROM startup_progress WHERE startup_id=@s20 AND summary='Progress update recorded for the shared workspace.');
INSERT INTO funding_requests(startup_id,amount,purpose,currency,status) SELECT @s20,3000000,'Product development and market expansion','INR','FUNDED' WHERE NOT EXISTS(SELECT 1 FROM funding_requests WHERE startup_id=@s20 AND purpose='Product development and market expansion');
SET @r20=(SELECT id FROM funding_requests WHERE startup_id=@s20 AND purpose='Product development and market expansion');
INSERT INTO investments(funding_request_id,investor_id,amount,currency,status) SELECT @r20,iv.id,600000,'INR','COMMITTED' FROM investors iv WHERE iv.user_id=@investor_user_id AND NOT EXISTS(SELECT 1 FROM investments WHERE funding_request_id=@r20 AND investor_id=iv.id);



-- Give the original live startup a completed milestone as well as its in-progress milestone.
INSERT INTO milestones(startup_id,title,due_date,status)
SELECT @startup_id,'Complete initial product validation',DATE_SUB(CURDATE(),INTERVAL 3 DAY),'COMPLETED'
WHERE NOT EXISTS(SELECT 1 FROM milestones WHERE startup_id=@startup_id AND title='Complete initial product validation');

-- Ensure every approved demo mentor has a real startup assignment and milestones to work with.
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by)
SELECT m.id,s.id,@admin_id
FROM mentors m JOIN startups s ON s.source_type='DEMO'
WHERE m.user_id=@mentor02 AND MOD(s.id,4)=0;
INSERT IGNORE INTO mentor_assignments(mentor_id,startup_id,assigned_by)
SELECT m.id,s.id,@admin_id
FROM mentors m JOIN startups s ON s.source_type='DEMO'
WHERE m.user_id=@mentor05 AND MOD(s.id,4)=1;

-- Ensure each operational startup has a meaningful team profile for Startup 360°.
INSERT INTO startup_teams(startup_id,member_name,member_role,email,joined_on)
SELECT s.id,CONCAT(s.name,' Operations Lead'),'Operations Lead',CONCAT('ops.',s.id,'@foundry.local'),CURDATE()
FROM startups s
WHERE s.source_type='DEMO'
  AND NOT EXISTS(SELECT 1 FROM startup_teams st WHERE st.startup_id=s.id AND st.member_role='Operations Lead');
INSERT INTO startup_teams(startup_id,member_name,member_role,email,joined_on)
SELECT s.id,CONCAT(s.name,' Product Lead'),'Product Lead',CONCAT('product.',s.id,'@foundry.local'),CURDATE()
FROM startups s
WHERE s.source_type='DEMO'
  AND NOT EXISTS(SELECT 1 FROM startup_teams st WHERE st.startup_id=s.id AND st.member_role='Product Lead');

-- Distribute the live applications across the five available programmes.
UPDATE applications a
JOIN startups s ON s.id=a.startup_id
SET a.program_id=CASE s.name
    WHEN 'SolarSutra' THEN @program_2
    WHEN 'AgriPulse' THEN @program_3
    WHEN 'MedBridge' THEN @program_4
    WHEN 'SkillSpring' THEN @program_5
    WHEN 'BlueCart' THEN @program_id
    WHEN 'ReCircle' THEN @program_2
    WHEN 'NammaTransit' THEN @program_3
    WHEN 'AquaSage' THEN @program_4
    WHEN 'CraftLink' THEN @program_5
    WHEN 'FarmGate' THEN @program_id
    WHEN 'CareNest' THEN @program_2
    WHEN 'WasteWise' THEN @program_3
    WHEN 'ByteBhasha' THEN @program_4
    WHEN 'RuralStack' THEN @program_5
    WHEN 'UrbanHarvest' THEN @program_id
    WHEN 'SafeSteps' THEN @program_2
    WHEN 'EduOrbit' THEN @program_3
    WHEN 'ChargeGrid' THEN @program_4
    WHEN 'SwasthyaNow' THEN @program_5
    WHEN 'LocalLedger' THEN @program_id
    ELSE a.program_id END
WHERE s.name IN ('SolarSutra','AgriPulse','MedBridge','SkillSpring','BlueCart','ReCircle','NammaTransit','AquaSage','CraftLink','FarmGate','CareNest','WasteWise','ByteBhasha','RuralStack','UrbanHarvest','SafeSteps','EduOrbit','ChargeGrid','SwasthyaNow','LocalLedger');

COMMIT;
