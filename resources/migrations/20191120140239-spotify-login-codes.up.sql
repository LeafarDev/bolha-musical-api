CREATE TABLE IF NOT EXISTS spotify_login_codes
(
    id         varchar(36) PRIMARY KEY, /* unhex(replace(uuid(),'-','')) */
    expires_at DATETIME     NOT NULL,
    checked_at TIMESTAMP    null,
    created_at TIMESTAMP    null
);