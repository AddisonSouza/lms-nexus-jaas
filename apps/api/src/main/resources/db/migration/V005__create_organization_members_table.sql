CREATE TABLE organization_members (
    id              CHAR(36)    NOT NULL,
    organization_id CHAR(36)    NOT NULL,
    user_id         CHAR(36)    NOT NULL,
    role            VARCHAR(50) NOT NULL,
    joined_at       DATETIME(6) NOT NULL,
    deleted_at      DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_org  FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id)         REFERENCES users (id),
    CONSTRAINT uq_member      UNIQUE (organization_id, user_id)
);
