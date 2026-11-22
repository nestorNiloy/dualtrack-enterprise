CREATE TABLE app_users (
 id BIGSERIAL PRIMARY KEY,
 email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE applications (
 id BIGSERIAL PRIMARY KEY,
 company_name VARCHAR(120) NOT NULL,
 position_title VARCHAR(160) NOT NULL,
 status VARCHAR(24) NOT NULL CHECK (status IN ('APPLIED','INTERVIEWING','REJECTED','OFFERED')),
 application_date DATE NOT NULL,
 notes TEXT,
 user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_app_owner_date ON applications(user_id,application_date DESC);
CREATE INDEX idx_app_owner_status ON applications(user_id,status);
CREATE TABLE application_status_history (
 id BIGSERIAL PRIMARY KEY,
 application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
 old_status VARCHAR(24),
 new_status VARCHAR(24) NOT NULL,
 changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 changed_by BIGINT NOT NULL REFERENCES app_users(id)
);
CREATE INDEX idx_history_application ON application_status_history(application_id,changed_at DESC);
