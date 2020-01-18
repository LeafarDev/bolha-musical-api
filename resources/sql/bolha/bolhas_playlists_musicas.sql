-- :name adicionar-track-playlist :! :n
-- :command :insert
-- :doc Insiro um usuário como membro em uma bolha
insert into bolhas_playlists_tracks (bolha_id, spotify_track_id, duration_ms, current_playing, created_at)
values (:bolha_id, :spotify_track_id, :duration_ms, :current_playing, :created_at);

-- :name get-tracks-by-bolha-id
-- :command :select
-- :doc Busca tracks de uma bolha
select * from bolhas_playlists_tracks
where  bolha_id = :bolha_id
        and deleted_at is null
order by id;

-- :name get-track-by-id
-- :command :select
-- :result :one
-- :doc Busca track pelo seu id
select * from bolhas_playlists_tracks
where  id = :id and deleted_at is null
order by id;

-- :name atualiza-estado-para-execucao-track :! :n
-- :command :update
-- :doc Atualiza colunas para controle de execução
update bolhas_playlists_tracks
set started_at = :agora,
    current_playing = 1
where id = :id;
-- :name atualiza-para-nao-execucao-track :! :n
-- :command :update
-- :doc Define como não executando
update bolhas_playlists_tracks
set current_playing = 0
where id = :id;

-- :name get-votos-track
-- :command :select
-- :doc Busca votos de uma track pelo seu track id interno
select * from bolhas_playlists_tracks_votos
where  track_interno_id = :track_interno_id and deleted_at is null
order by id;

-- :name adicionar-voto-track-playlist :! :n
-- :command :insert
-- :doc Insiro um novo voto para um track de uma bolha
 INSERT INTO bolhas_playlists_tracks_votos
            (track_interno_id,
             user_id,
             cimavoto,
             created_by,
             created_at)
VALUES      (:track_interno_id,
             :user_id,
             :cimavoto,
             :created_by,
             :created_at);

-- :name remover-voto-track-playlist :! :n
-- :command :update
-- :doc Remove voto de um usuário
UPDATE bolhas_playlists_tracks_votos
SET    deleted_at = :deleted_at
WHERE  track_interno_id = :track_interno_id
       AND user_id = :user_id;