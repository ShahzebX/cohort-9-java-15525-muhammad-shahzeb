-- V6: Enforce mandatory contact membership for email and phone rows.
--     Every email and every phone must belong to a contact.
--
-- Safety note: the previous version of this migration deleted orphaned email
-- and phone rows (DELETE ... WHERE contact_id IS NULL). Silently discarding
-- or cascading away that data is not acceptable. Instead, a preflight check
-- aborts the migration with a clear message if orphaned rows exist, so they
-- can be reviewed, re-attached, or explicitly archived by a human before the
-- NOT NULL constraints are enforced.

DELIMITER $$
CREATE PROCEDURE v6_preflight_orphaned_contact_children()
BEGIN
    IF EXISTS (SELECT 1 FROM email WHERE contact_id IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V6 preflight failed: email rows with contact_id IS NULL exist. Preserve/remediate/archive them before enforcing email.contact_id NOT NULL.';
    END IF;
    IF EXISTS (SELECT 1 FROM phone WHERE contact_id IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V6 preflight failed: phone rows with contact_id IS NULL exist. Preserve/remediate/archive them before enforcing phone.contact_id NOT NULL.';
    END IF;
END$$
DELIMITER ;

CALL v6_preflight_orphaned_contact_children();
DROP PROCEDURE v6_preflight_orphaned_contact_children;

ALTER TABLE email MODIFY COLUMN contact_id INT NOT NULL;
ALTER TABLE phone MODIFY COLUMN contact_id INT NOT NULL;
