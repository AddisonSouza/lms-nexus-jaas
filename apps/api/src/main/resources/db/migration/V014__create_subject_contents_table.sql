CREATE TABLE subject_contents (
    id              VARCHAR(36)   NOT NULL,
    topic_id        VARCHAR(36)   NOT NULL,
    organization_id VARCHAR(36)   NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    content_type    VARCHAR(20)   NOT NULL,
    external_url    TEXT          NULL,
    file_key        VARCHAR(512)  NULL,
    description     TEXT          NULL,
    position        INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_contents_topic FOREIGN KEY (topic_id) REFERENCES subject_topics (id),
    CONSTRAINT fk_contents_org   FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
