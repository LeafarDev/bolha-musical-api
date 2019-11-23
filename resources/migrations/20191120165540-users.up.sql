CREATE TABLE IF NOT EXISTS users
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email varchar(256) not null,
    password varchar(191) null,
    is_active tinyint(1) default 1 not null,
    rocket_chat_auth_token varchar(100) null,
    rocket_chat_user_id varchar(50) null,
    spotify_client_id  varchar(255) null,
    spotify_access_token varchar(255) null,
    spotify_scope varchar(1000) null,
    spotify_token_expires_at datetime null,
    spotify_refresh_token  varchar(255) null,
    spotify_last_state varchar(36) null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
