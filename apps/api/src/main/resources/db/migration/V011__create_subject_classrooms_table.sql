CREATE TABLE subject_classrooms (
    subject_id   VARCHAR(36) NOT NULL,
    classroom_id VARCHAR(36) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subject_id, classroom_id),
    CONSTRAINT fk_sc_subject   FOREIGN KEY (subject_id)   REFERENCES subjects   (id),
    CONSTRAINT fk_sc_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms (id)
);
