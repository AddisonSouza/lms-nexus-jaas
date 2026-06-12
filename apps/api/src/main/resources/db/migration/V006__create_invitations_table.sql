CREATE TABLE invitations (
    id              CHAR(36)     NOT NULL,
    organization_id CHAR(36)     NOT NULL,
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    token           CHAR(36)     NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    invited_by      CHAR(36)     NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_invitation_token UNIQUE (token),
    CONSTRAINT fk_inv_org    FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_inv_inviter FOREIGN KEY (invited_by)     REFERENCES users (id)
);
