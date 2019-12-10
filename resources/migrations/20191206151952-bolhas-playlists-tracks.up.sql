create TABLE IF NOT EXISTS bolhas_playlists_tracks
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    bolha_id int not null,
    spotify_track_id varchar(30) not null,
    duration_ms int not null,
    started_at DATETIME,
    current_playing int default 0,
    cimavotos int not null default 0,
    baixavotos int not null default 0,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
