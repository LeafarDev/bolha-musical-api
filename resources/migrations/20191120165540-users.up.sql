CREATE TABLE IF NOT EXISTS users
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email varchar(256) not null,
    password varchar(191) null,
    rocket_chat_id varchar(100) null,
    rocket_chat_password varchar(255) null,
    mostrar_localizacao_mapa tinyint(1) default 1 not null,
    tocar_track_automaticamente  tinyint(1) default 1 not null,
    is_active tinyint(1) default 1 not null,
    spotify_client_id  varchar(255) null,
    spotify_access_token varchar(255) null,
    spotify_scope varchar(1000) null,
    spotify_token_expires_at datetime null,
    spotify_refresh_token  varchar(255) null,
    spotify_last_state varchar(36) null,
    spotify_current_device varchar(255) null,
    country_code  varchar(2) null,
    language_code varchar(10) null,
    ultima_localizacao POINT null, /* utilizado quando for bolha fixa*/
    data_ultima_localizacao DATETIME null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
