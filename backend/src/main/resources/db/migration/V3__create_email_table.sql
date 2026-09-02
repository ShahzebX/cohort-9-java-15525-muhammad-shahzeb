-- V3: Create the email table matching the Email entity

CREATE TABLE email (
    id         INT          NOT NULL AUTO_INCREMENT,
    email      VARCHAR(255) NULL,
    label      VARCHAR(100) NULL,
    contact_id INT          NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_email_contact FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
