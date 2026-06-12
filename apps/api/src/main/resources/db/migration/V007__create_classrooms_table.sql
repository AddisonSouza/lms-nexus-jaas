CREATE TABLE classrooms (
    id              VARCHAR(36)           NOT NULL,
    organization_id VARCHAR(36)           NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    description     TEXT,
    academic_period VARCHAR(100)          NOT NULL,
    status          ENUM('ACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    invite_code     VARCHAR(6)            NOT NULL,
    created_at      TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP             NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_classrooms_invite_code (invite_code),
    CONSTRAINT fk_classrooms_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
