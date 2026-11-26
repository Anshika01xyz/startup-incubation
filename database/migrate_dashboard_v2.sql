USE startup_platform;

-- Upgrade existing installations safely: add incubator account ownership only when absent.
SET @has_user_id = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'incubators' AND COLUMN_NAME = 'user_id'
);
SET @ddl = IF(@has_user_id = 0,
  'ALTER TABLE incubators ADD COLUMN user_id BIGINT NULL UNIQUE, ADD CONSTRAINT fk_incubator_user FOREIGN KEY (user_id) REFERENCES users(id)',
  'SELECT ''incubators.user_id already exists'' AS migration_status');
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

INSERT IGNORE INTO roles(role_name) VALUES ('INCUBATOR');
