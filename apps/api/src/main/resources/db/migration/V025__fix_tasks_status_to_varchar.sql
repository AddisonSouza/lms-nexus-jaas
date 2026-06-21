-- Same ENUM-vs-VARCHAR mismatch as V024, for the tasks table: the JPA entity maps
-- `status` as a String column (VARCHAR(20)), but V015 created it as an ENUM.
ALTER TABLE tasks
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
