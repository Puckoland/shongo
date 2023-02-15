/**
 * 2023-02-15: tag can now hold additional data
 */
BEGIN TRANSACTION;

CREATE TYPE tag_type AS ENUM(
    'DEFAULT',
    'NOTIFY_EMAIL',
    'RESERVATION_DATA'
);

ALTER TABLE tag
    ADD COLUMN type tag_type NOT NULL DEFAULT 'DEFAULT',
    ADD COLUMN data jsonb;

COMMIT TRANSACTION;
