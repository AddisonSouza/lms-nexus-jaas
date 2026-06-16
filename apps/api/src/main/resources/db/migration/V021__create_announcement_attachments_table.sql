CREATE TABLE announcement_attachments (
    id               VARCHAR(36)  NOT NULL,
    announcement_id  VARCHAR(36)  NOT NULL,
    file_key         VARCHAR(512) NULL,
    original_name    VARCHAR(255) NULL,
    mime_type        VARCHAR(127) NULL,
    size_bytes       BIGINT       NULL,
    external_url     TEXT         NULL,
    link_title       VARCHAR(255) NULL,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_announcement_attachments_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id),
    INDEX idx_announcement_attachments_announcement (announcement_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
