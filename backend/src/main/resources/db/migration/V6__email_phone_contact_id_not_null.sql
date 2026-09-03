-- V6: Enforce mandatory contact membership for email and phone rows.
--     Every email and every phone must belong to a contact.

-- Remove orphaned emails that would violate the new constraint.
DELETE FROM email WHERE contact_id IS NULL;

-- Remove orphaned phones that would violate the new constraint.
DELETE FROM phone WHERE contact_id IS NULL;

ALTER TABLE email MODIFY COLUMN contact_id INT NOT NULL;
ALTER TABLE phone MODIFY COLUMN contact_id INT NOT NULL;
