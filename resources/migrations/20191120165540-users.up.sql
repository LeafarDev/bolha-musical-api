CREATE TABLE IF NOT EXISTS users
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email varchar(256) not null,
    password varchar(191) null,
    is_active tinyint(1) default 1 not null,
    rocket_chat_authToken varchar(100) null,
    rocket_chat_userId varchar(50) null,
    spotify_client_id varchar(100) null,
    spotify_access_token varchar(100) null,
    spotify_token_type varchar(100) null,
    spotify_scope varchar(100) null,
    spotify_token_expires_in int null,
    spotify_refresh_token  varchar(100) null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
