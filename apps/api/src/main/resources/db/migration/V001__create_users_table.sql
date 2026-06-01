CREATE TABLE users (
    id            VARCHAR(36)   NOT NULL,
    full_name     VARCHAR(150)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(72)   NOT NULL,
    status        ENUM('PENDING_CONFIRMATION', 'ACTIVE', 'SUSPENDED')
                                NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
);
