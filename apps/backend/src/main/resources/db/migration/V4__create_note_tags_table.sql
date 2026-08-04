CREATE TABLE note_tags (
  user_id BIGINT NOT NULL,
  note_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_note_tags 
    PRIMARY KEY (note_id, tag_id),

  CONSTRAINT fk_note_tags_note_owner
    FOREIGN KEY (note_id, user_id)
    REFERENCES notes(id, user_id) 
    ON DELETE CASCADE,

  CONSTRAINT fk_note_tags_tag_owner
    FOREIGN KEY (tag_id, user_id)
    REFERENCES tags(id, user_id) 
    ON DELETE CASCADE
);

CREATE INDEX idx_note_tags_tag_id ON note_tags (tag_id);