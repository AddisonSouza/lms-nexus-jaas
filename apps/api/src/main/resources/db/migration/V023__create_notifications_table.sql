CREATE TABLE notifications (
    id               VARCHAR(36)  NOT NULL,
    user_id          VARCHAR(36)  NOT NULL,
    organization_id  VARCHAR(36)  NOT NULL,
    type             VARCHAR(40)  NOT NULL,
    reference_id     VARCHAR(36)  NOT NULL,
    title            VARCHAR(120) NOT NULL,
    message          VARCHAR(500) NOT NULL,
    action_link      VARCHAR(255) NOT NULL,
    read_at          DATETIME(6)  NULL,
    created_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)         REFERENCES users(id),
    CONSTRAINT fk_notifications_org  FOREIGN KEY (organization_id) REFERENCES organizations(id),
    INDEX idx_notifications_user_created (user_id, created_at),
    INDEX idx_notifications_user_unread  (user_id, read_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
