ALTER TABLE task_submissions
    ADD COLUMN grade   DECIMAL(5, 2) NULL,
    ADD COLUMN feedback LONGTEXT NULL;
