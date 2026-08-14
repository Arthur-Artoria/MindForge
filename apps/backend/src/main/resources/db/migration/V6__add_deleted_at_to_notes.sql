ALTER TABLE notes
ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX idx_notes_active_user_id
ON notes (user_id)
WHERE deleted_at IS NULL;