CREATE TABLE organizations (
    id          CHAR(36)     NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id    CHAR(36)     NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    deleted_at  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_org_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);
