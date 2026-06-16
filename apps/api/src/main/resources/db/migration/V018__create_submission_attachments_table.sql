CREATE TABLE submission_attachments (
    id              VARCHAR(36)  NOT NULL,
    submission_id   VARCHAR(36)  NOT NULL,
    file_key        VARCHAR(512) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    mime_type       VARCHAR(127) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sub_attachments_submission FOREIGN KEY (submission_id) REFERENCES task_submissions(id),
    INDEX idx_sub_attachments_submission (submission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
