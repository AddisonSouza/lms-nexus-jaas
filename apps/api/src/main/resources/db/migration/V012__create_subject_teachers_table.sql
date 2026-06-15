CREATE TABLE subject_teachers (
    subject_id VARCHAR(36) NOT NULL,
    member_id  VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subject_id, member_id),
    CONSTRAINT fk_st_subject FOREIGN KEY (subject_id) REFERENCES subjects              (id),
    CONSTRAINT fk_st_member  FOREIGN KEY (member_id)  REFERENCES organization_members  (id)
);
