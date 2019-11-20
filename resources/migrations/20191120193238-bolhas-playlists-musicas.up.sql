CREATE TABLE IF NOT EXISTS bolhas_playlists_musicas
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    tocando_atualmente tinyint(1) default 0 not null,
    duration_ms int,
    starts_at datetime, /* conto o ends_at, com base no duration_ms, apos finalizar mando todos os membros tocarem o próximo */
   /* tambem permite que eu possa manter os usuários sincronizados com ms atual da musica */
    ordem int not null,
    track_id varchar(100) null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
