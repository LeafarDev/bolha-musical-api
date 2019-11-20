CREATE TABLE IF NOT EXISTS spotify_login_codes
(
    id         binary(16) PRIMARY KEY, /* unhex(replace(uuid(),'-','')) */
    codigo     VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME     NOT NULL UNIQUE,
    checkin    BOOLEAN DEFAULT false,
    checked_at TIMESTAMP    null,
    created_at TIMESTAMP    null
);