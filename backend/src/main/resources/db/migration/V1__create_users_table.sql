-- V1: Create the users table matching the User entity
-- Column names follow SpringPhysicalNamingStrategy (camelCase -> snake_case).

CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NULL,
    phone         VARCHAR(50)  NULL,
    first_name    VARCHAR(100) NULL,
    last_name     VARCHAR(100) NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
