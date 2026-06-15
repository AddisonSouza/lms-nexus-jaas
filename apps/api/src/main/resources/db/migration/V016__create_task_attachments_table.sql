CREATE TABLE task_attachments (
    id            VARCHAR(36)  NOT NULL,
    task_id       VARCHAR(36)  NOT NULL,
    file_key      VARCHAR(512) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    mime_type     VARCHAR(127) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    created_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_attachments_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    INDEX idx_task_attachments_task (task_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
