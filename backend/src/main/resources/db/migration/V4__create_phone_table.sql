-- V4: Create the phone table matching the Phone entity

CREATE TABLE phone (
    id           INT          NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(50)  NULL,
    label        VARCHAR(100) NULL,
    contact_id   INT          NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_phone_contact FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
