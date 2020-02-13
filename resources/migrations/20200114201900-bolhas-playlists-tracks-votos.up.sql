create TABLE IF NOT EXISTS bolhas_playlists_tracks_votos
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    track_interno_id int not null,
    user_id int not null,
    cimavoto tinyint(1) not null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
