-- V2: Create the contact table matching the Contact entity

CREATE TABLE contact (
    id         INT          NOT NULL AUTO_INCREMENT,
    user_id    INT          NULL,
    first_name VARCHAR(100) NULL,
    last_name  VARCHAR(100) NULL,
    title      VARCHAR(100) NULL,
    created_at TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_contact_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
