DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create index for faster token lookups
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_token(token);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    faculty_advisor_id BIGINT REFERENCES users(id)
);

-- Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- User Roles Junction Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Internships Table
CREATE TABLE IF NOT EXISTS internships (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    advisor_id BIGINT REFERENCES users(id),
    company_name VARCHAR(255) NOT NULL,
    company_address TEXT NOT NULL,
    company_phone VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    work_days INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    rejection_reason TEXT,
    type VARCHAR(50) NOT NULL,
    parental_insurance_coverage BOOLEAN NOT NULL,
    company_iban VARCHAR(50),
    bank_name VARCHAR(255),
    bank_branch VARCHAR(255),
    is_paid BOOLEAN NOT NULL,
    insurance_support BOOLEAN NOT NULL,
    description TEXT,
    document_path VARCHAR(255)
);

-- Application Working Days Table
CREATE TABLE IF NOT EXISTS application_working_days (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL REFERENCES internships(id),
    date DATE NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    is_working_day BOOLEAN NOT NULL,
    note TEXT
);

-- Application Approvals Table
CREATE TABLE IF NOT EXISTS application_approvals (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL REFERENCES internships(id),
    approver_id BIGINT NOT NULL REFERENCES users(id),
    approver_role VARCHAR(50) NOT NULL,
    action_date TIMESTAMP NOT NULL,
    result_status VARCHAR(50) NOT NULL,
    comments TEXT,
    is_approved BOOLEAN NOT NULL
);

-- SGK Declarations Table
CREATE TABLE IF NOT EXISTS sgk_declarations (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL REFERENCES internships(id),
    declaration_number VARCHAR(50) NOT NULL,
    generation_date TIMESTAMP NOT NULL,
    document_path VARCHAR(255) NOT NULL,
    is_processed BOOLEAN NOT NULL,
    process_notes TEXT
);

-- Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL
); 