-- V5: Enforce mandatory contact ownership (contact.user_id must not be NULL)

-- Remove any orphaned contacts that would violate the new constraint.
DELETE FROM contact WHERE user_id IS NULL;

ALTER TABLE contact MODIFY COLUMN user_id INT NOT NULL;
