CREATE TABLE subjects (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(20),
    description     TEXT,
    workload_hours  INT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_subjects_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
