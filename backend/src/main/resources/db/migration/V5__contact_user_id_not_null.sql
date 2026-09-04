-- V5: Enforce mandatory contact ownership (contact.user_id must not be NULL)
--
-- Safety note: the previous version of this migration deleted orphaned
-- contacts (DELETE FROM contact WHERE user_id IS NULL). Silently discarding
-- user data is not acceptable. Instead, a preflight check aborts the migration
-- with a clear message if any contact lacks an owner, so the offending rows
-- can be reviewed/remediated or explicitly archived by a human before the NOT
-- NULL constraint is enforced. This fails loudly rather than losing data.

DELIMITER $$
CREATE PROCEDURE v5_preflight_orphaned_contacts()
BEGIN
    IF EXISTS (SELECT 1 FROM contact WHERE user_id IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V5 preflight failed: contact rows with user_id IS NULL exist. Remediate or archive them before enforcing contact.user_id NOT NULL.';
    END IF;
END$$
DELIMITER ;

CALL v5_preflight_orphaned_contacts();
DROP PROCEDURE v5_preflight_orphaned_contacts;

ALTER TABLE contact MODIFY COLUMN user_id INT NOT NULL;
