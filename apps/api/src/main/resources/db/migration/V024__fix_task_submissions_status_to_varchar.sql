-- The JPA entity maps `status` as a String column (VARCHAR(20)), but V017 created
-- it as an ENUM, which fails Hibernate's boot-time schema validation. Align the
-- column type with the entity mapping.
ALTER TABLE task_submissions
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED';
