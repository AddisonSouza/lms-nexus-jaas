ALTER TABLE announcements
    MODIFY COLUMN created_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL,
    MODIFY COLUMN deleted_at DATETIME(6) NULL;

ALTER TABLE announcement_attachments
    MODIFY COLUMN created_at DATETIME(6) NOT NULL;
