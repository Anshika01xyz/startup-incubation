-- =====================================================================
-- auth_schema.sql  |  Owner: Shaffy  |  Shared tables: users, roles, user_roles
-- Run order: this file runs BEFORE module schemas (everything references users).
-- Requires MySQL 8.0.16+ (CHECK constraints are enforced).
-- =====================================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id      INT AUTO_INCREMENT PRIMARY KEY,
    role_name    VARCHAR(30)  NOT NULL UNIQUE,
    description  VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,          -- BCrypt hash (60 chars). Never plaintext.
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at  DATETIME     NULL,
    CONSTRAINT chk_users_email CHECK (email LIKE '%_@_%._%')
) ENGINE=InnoDB;

-- Junction table: a user can hold several roles.
CREATE TABLE IF NOT EXISTS user_roles (
    user_id  INT NOT NULL,
    role_id  INT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB;

CREATE INDEX idx_users_active ON users(is_active);

-- Seed roles (names MUST match com.startup.platform.auth.Role enum)
INSERT IGNORE INTO roles (role_name, description) VALUES
 ('ADMIN',             'Platform administrator'),
 ('FOUNDER',           'Startup founder'),
 ('INCUBATOR_MANAGER', 'Reviews applications, decides, assigns mentors, manages milestones'),
 ('EVALUATOR',         'Scores applications against criteria'),
 ('MENTOR',            'Guides accepted startups and gives milestone feedback'),
 ('INVESTOR',          'Browses approved startups and records investments');

-- NOTE: demo users are created by a Java seed utility so passwords are
-- hashed with BCrypt. Do not paste fake hashes here.
