CREATE TABLE announcements (
    id               VARCHAR(36)  NOT NULL,
    classroom_id     VARCHAR(36)  NOT NULL,
    organization_id  VARCHAR(36)  NOT NULL,
    author_id        VARCHAR(36)  NOT NULL,
    content          LONGTEXT     NOT NULL,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    deleted_at       DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_announcements_classroom FOREIGN KEY (classroom_id)     REFERENCES classrooms(id),
    CONSTRAINT fk_announcements_org       FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_announcements_author    FOREIGN KEY (author_id)       REFERENCES users(id),
    INDEX idx_announcements_classroom (classroom_id, created_at),
    INDEX idx_announcements_deleted   (deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
