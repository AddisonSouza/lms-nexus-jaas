CREATE TABLE subject_topics (
    id              VARCHAR(36)  NOT NULL,
    subject_id      VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    position        INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_topics_org     FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
