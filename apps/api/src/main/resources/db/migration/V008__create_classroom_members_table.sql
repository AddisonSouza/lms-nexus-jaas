CREATE TABLE classroom_members (
    id              VARCHAR(36)              NOT NULL,
    classroom_id    VARCHAR(36)              NOT NULL,
    user_id         VARCHAR(36)              NOT NULL,
    organization_id VARCHAR(36)              NOT NULL,
    role            ENUM('PROFESSOR','ALUNO') NOT NULL,
    joined_at       TIMESTAMP                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP                NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_classroom_member (classroom_id, user_id),
    CONSTRAINT fk_cm_classroom FOREIGN KEY (classroom_id)    REFERENCES classrooms     (id),
    CONSTRAINT fk_cm_user      FOREIGN KEY (user_id)         REFERENCES users          (id),
    CONSTRAINT fk_cm_org       FOREIGN KEY (organization_id) REFERENCES organizations  (id)
);
