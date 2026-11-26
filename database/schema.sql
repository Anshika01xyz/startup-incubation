CREATE DATABASE IF NOT EXISTS startup_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE startup_platform;

CREATE TABLE IF NOT EXISTS users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(120) NOT NULL, email VARCHAR(180) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL, status ENUM('PENDING','ACTIVE','REJECTED','DISABLED') NOT NULL DEFAULT 'ACTIVE',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS roles (
 id INT PRIMARY KEY AUTO_INCREMENT, role_name VARCHAR(40) NOT NULL UNIQUE
);
INSERT IGNORE INTO roles(role_name) VALUES ('ADMIN'),('FOUNDER'),('MENTOR'),('EVALUATOR'),('INVESTOR'),('INCUBATOR');
CREATE TABLE IF NOT EXISTS user_roles (
 user_id BIGINT NOT NULL, role_id INT NOT NULL, PRIMARY KEY(user_id,role_id),
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(role_id) REFERENCES roles(id)
);
CREATE TABLE IF NOT EXISTS founders (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, phone VARCHAR(30), bio TEXT,
 FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS startups (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(180) NOT NULL, sector VARCHAR(100), city VARCHAR(100),
 description TEXT, website VARCHAR(255), founded_year INT, stage VARCHAR(50),
 founder_user_id BIGINT NOT NULL, status ENUM('DRAFT','SUBMITTED','INCUBATED','FUNDRAISING','CLOSED') DEFAULT 'DRAFT',
 source_type ENUM('REGISTERED','IMPORTED','DEMO') NOT NULL DEFAULT 'REGISTERED',
 source_reference VARCHAR(500), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(founder_user_id) REFERENCES users(id), INDEX idx_startup_sector(sector), INDEX idx_startup_status(status)
);
CREATE TABLE IF NOT EXISTS startup_documents (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, file_name VARCHAR(255) NOT NULL,
 stored_path VARCHAR(600) NOT NULL, document_type VARCHAR(60), uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(startup_id) REFERENCES startups(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS incubators (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(180) NOT NULL UNIQUE, city VARCHAR(100), description TEXT, user_id BIGINT UNIQUE,
 FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS programs (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, incubator_id BIGINT NOT NULL, name VARCHAR(180) NOT NULL,
 duration_months INT NOT NULL CHECK(duration_months BETWEEN 1 AND 36), FOREIGN KEY(incubator_id) REFERENCES incubators(id)
);
CREATE TABLE IF NOT EXISTS applications (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, program_id BIGINT NOT NULL,
 application_date DATE NOT NULL, status ENUM('SUBMITTED','UNDER_REVIEW','ACCEPTED','REJECTED') DEFAULT 'SUBMITTED',
 UNIQUE(startup_id,program_id), FOREIGN KEY(startup_id) REFERENCES startups(id),
 FOREIGN KEY(program_id) REFERENCES programs(id)
);
CREATE TABLE IF NOT EXISTS evaluators (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, expertise VARCHAR(180),
 FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS evaluations (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, application_id BIGINT NOT NULL UNIQUE, evaluator_user_id BIGINT NOT NULL,
 innovation_score DECIMAL(4,2) NOT NULL, market_score DECIMAL(4,2) NOT NULL,
 feasibility_score DECIMAL(4,2) NOT NULL, team_score DECIMAL(4,2) NOT NULL,
 comments TEXT, recommendation ENUM('ACCEPT','REJECT','REVIEW') NOT NULL,
 evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(application_id) REFERENCES applications(id), FOREIGN KEY(evaluator_user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS mentors (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, expertise VARCHAR(180),
 experience_years INT DEFAULT 0 CHECK(experience_years >= 0), approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
 FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS mentor_assignments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, mentor_id BIGINT NOT NULL, startup_id BIGINT NOT NULL,
 assigned_by BIGINT NOT NULL, assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(mentor_id) REFERENCES mentors(id), FOREIGN KEY(startup_id) REFERENCES startups(id),
 FOREIGN KEY(assigned_by) REFERENCES users(id), UNIQUE(mentor_id,startup_id)
);
CREATE TABLE IF NOT EXISTS milestones (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, title VARCHAR(180) NOT NULL,
 due_date DATE NOT NULL, status ENUM('NOT_STARTED','IN_PROGRESS','COMPLETED','BLOCKED') DEFAULT 'NOT_STARTED',
 FOREIGN KEY(startup_id) REFERENCES startups(id)
);
CREATE TABLE IF NOT EXISTS milestone_updates (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, milestone_id BIGINT NOT NULL, update_text TEXT NOT NULL,
 updated_by BIGINT NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(milestone_id) REFERENCES milestones(id), FOREIGN KEY(updated_by) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS investors (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, organization VARCHAR(180),
 ticket_min DECIMAL(15,2), ticket_max DECIMAL(15,2), FOREIGN KEY(user_id) REFERENCES users(id), CHECK(ticket_min IS NULL OR ticket_min >= 0), CHECK(ticket_max IS NULL OR ticket_max >= 0), CHECK(ticket_min IS NULL OR ticket_max IS NULL OR ticket_max >= ticket_min)
);
CREATE TABLE IF NOT EXISTS investor_interests (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, investor_id BIGINT NOT NULL, sector VARCHAR(100) NOT NULL,
 UNIQUE(investor_id,sector), FOREIGN KEY(investor_id) REFERENCES investors(id)
);
CREATE TABLE IF NOT EXISTS investor_stage_preferences (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, investor_id BIGINT NOT NULL, preferred_stage VARCHAR(60) NOT NULL,
 UNIQUE(investor_id,preferred_stage), FOREIGN KEY(investor_id) REFERENCES investors(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS funding_requests (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, amount DECIMAL(15,2) NOT NULL,
 purpose TEXT, currency CHAR(3) NOT NULL DEFAULT 'INR', status ENUM('OPEN','IN_REVIEW','FUNDED','CLOSED') DEFAULT 'OPEN', CHECK(amount > 0),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(startup_id) REFERENCES startups(id)
);
CREATE TABLE IF NOT EXISTS funding_documents (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, funding_request_id BIGINT NOT NULL, file_name VARCHAR(255),
 stored_path VARCHAR(600), FOREIGN KEY(funding_request_id) REFERENCES funding_requests(id)
);
CREATE TABLE IF NOT EXISTS investments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, funding_request_id BIGINT NOT NULL, investor_id BIGINT NOT NULL,
 amount DECIMAL(15,2) NOT NULL CHECK(amount > 0), currency CHAR(3) NOT NULL DEFAULT 'INR', status ENUM('COMMITTED','RECEIVED','CANCELLED') DEFAULT 'COMMITTED',
 invested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(funding_request_id) REFERENCES funding_requests(id),
 FOREIGN KEY(investor_id) REFERENCES investors(id)
);
CREATE TABLE IF NOT EXISTS investment_transactions (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, investment_id BIGINT NOT NULL, amount DECIMAL(15,2) NOT NULL CHECK(amount > 0),
 currency CHAR(3) NOT NULL DEFAULT 'INR', transaction_date DATE NOT NULL, reference_code VARCHAR(120), FOREIGN KEY(investment_id) REFERENCES investments(id)
);
CREATE TABLE IF NOT EXISTS external_funding_rounds (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, round_id VARCHAR(40), announced_date DATE, announcement_year INT,
 startup_name VARCHAR(220) NOT NULL, company_domain VARCHAR(180), country VARCHAR(80), sector VARCHAR(120),
 city VARCHAR(120), founded_year INT, funding_stage VARCHAR(100), amount_usd DECIMAL(20,2),
 amount_status VARCHAR(40), lead_investor VARCHAR(220), investor_count INT, news_mentions INT, news_source VARCHAR(180),
 source_url VARCHAR(600), news_excerpt TEXT, profile_url VARCHAR(600),
 dataset_key VARCHAR(255) NOT NULL UNIQUE, imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_external_name(startup_name), INDEX idx_external_sector(sector), INDEX idx_external_date(announced_date)
);
CREATE TABLE IF NOT EXISTS activity_logs (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, actor_user_id BIGINT, action VARCHAR(100) NOT NULL,
 entity_type VARCHAR(80), entity_id BIGINT, details TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(actor_user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS notifications (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, message VARCHAR(500) NOT NULL,
 is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS startup_progress (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, progress_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
 summary TEXT, updated_by BIGINT NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(startup_id) REFERENCES startups(id) ON DELETE CASCADE, FOREIGN KEY(updated_by) REFERENCES users(id),
 CONSTRAINT chk_progress CHECK(progress_percent BETWEEN 0 AND 100)
);
CREATE OR REPLACE VIEW v_milestone_health AS
SELECT m.id, m.startup_id, s.name AS startup_name, m.title, m.due_date, m.status,
 CASE WHEN m.status='COMPLETED' THEN 'COMPLETED'
      WHEN m.due_date < CURRENT_DATE THEN 'OVERDUE'
      WHEN DATEDIFF(m.due_date,CURRENT_DATE)<=7 THEN 'DUE_SOON'
      ELSE 'ON_TRACK' END AS health
FROM milestones m JOIN startups s ON s.id=m.startup_id;
CREATE OR REPLACE VIEW v_funding_summary AS
SELECT s.id AS startup_id, s.name,
       COALESCE(fr.request_count,0) AS request_count,
       COALESCE(fr.requested_amount,0) AS requested_amount,
       COALESCE(inv.committed_amount,0) AS committed_amount
FROM startups s
LEFT JOIN (
    SELECT startup_id, COUNT(*) AS request_count, SUM(amount) AS requested_amount
    FROM funding_requests GROUP BY startup_id
) fr ON fr.startup_id=s.id
LEFT JOIN (
    SELECT fr.startup_id, SUM(i.amount) AS committed_amount
    FROM investments i JOIN funding_requests fr ON fr.id=i.funding_request_id
    WHERE i.status<>'CANCELLED' GROUP BY fr.startup_id
) inv ON inv.startup_id=s.id;

CREATE TABLE IF NOT EXISTS startup_founders (
 startup_id BIGINT NOT NULL, founder_id BIGINT NOT NULL, founder_role VARCHAR(80) NOT NULL DEFAULT 'Co-founder',
 equity_percent DECIMAL(5,2), PRIMARY KEY(startup_id,founder_id),
 FOREIGN KEY(startup_id) REFERENCES startups(id) ON DELETE CASCADE,
 FOREIGN KEY(founder_id) REFERENCES founders(id) ON DELETE CASCADE,
 CONSTRAINT chk_equity CHECK(equity_percent IS NULL OR equity_percent BETWEEN 0 AND 100)
);
CREATE TABLE IF NOT EXISTS startup_teams (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, startup_id BIGINT NOT NULL, member_name VARCHAR(140) NOT NULL,
 member_role VARCHAR(100) NOT NULL, email VARCHAR(180), joined_on DATE,
 FOREIGN KEY(startup_id) REFERENCES startups(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS evaluation_criteria (
 id INT PRIMARY KEY AUTO_INCREMENT, criterion_name VARCHAR(80) NOT NULL UNIQUE,
 description VARCHAR(300), max_score DECIMAL(4,2) NOT NULL DEFAULT 10
);
INSERT IGNORE INTO evaluation_criteria(criterion_name,description,max_score) VALUES
('Innovation','Originality and differentiation of the solution',10),
('Market','Market size, customer need and opportunity',10),
('Feasibility','Technical and operational feasibility',10),
('Team','Founder capability and execution readiness',10);
CREATE TABLE IF NOT EXISTS evaluation_scores (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, evaluation_id BIGINT NOT NULL, criterion_id INT NOT NULL,
 score DECIMAL(4,2) NOT NULL, UNIQUE(evaluation_id,criterion_id),
 FOREIGN KEY(evaluation_id) REFERENCES evaluations(id) ON DELETE CASCADE,
 FOREIGN KEY(criterion_id) REFERENCES evaluation_criteria(id),
 CONSTRAINT chk_eval_score CHECK(score BETWEEN 0 AND 10)
);

CREATE OR REPLACE VIEW v_startup_360 AS
SELECT s.id, s.name, s.sector, s.city, s.stage, s.status, s.source_type,
       COALESCE((SELECT COUNT(*) FROM applications a WHERE a.startup_id=s.id),0) AS application_count,
       COALESCE((SELECT COUNT(*) FROM mentor_assignments ma WHERE ma.startup_id=s.id),0) AS mentor_count,
       COALESCE((SELECT COUNT(*) FROM milestones m WHERE m.startup_id=s.id),0) AS milestone_count,
       COALESCE((SELECT COUNT(*) FROM milestones m WHERE m.startup_id=s.id AND m.status='COMPLETED'),0) AS completed_milestones,
       COALESCE((SELECT SUM(fr.amount) FROM funding_requests fr WHERE fr.startup_id=s.id),0) AS funding_requested,
       COALESCE((SELECT SUM(i.amount) FROM investments i JOIN funding_requests fr ON fr.id=i.funding_request_id WHERE fr.startup_id=s.id AND i.status<>'CANCELLED'),0) AS investment_committed,
       COALESCE((SELECT ROUND(AVG((e.innovation_score+e.market_score+e.feasibility_score+e.team_score)/4)*10,1)
                 FROM evaluations e JOIN applications a ON a.id=e.application_id WHERE a.startup_id=s.id),0) AS readiness_score,
       COALESCE((SELECT COUNT(*) FROM startup_teams st WHERE st.startup_id=s.id),0) AS team_count,
       COALESCE((SELECT sp.progress_percent FROM startup_progress sp WHERE sp.startup_id=s.id ORDER BY sp.updated_at DESC LIMIT 1),0) AS latest_progress,
       COALESCE((SELECT MAX(sp.updated_at) FROM startup_progress sp WHERE sp.startup_id=s.id),s.created_at) AS updated_at
FROM startups s;
