-- :name adicionar-track-playlist :! :n
-- :command :insert
-- :doc Insiro um usuário como membro em uma bolha
insert into bolhas_playlists_tracks (bolha_id, spotify_track_id, duration_ms, current_playing, created_at)
values (:bolha_id, :spotify_track_id, :duration_ms, :current_playing, :created_at);

-- :name get-tracks-by-bolha-id
-- :command :select
-- :doc Busca usuário pelo seu email
select * from bolhas_playlists_tracks
where  bolha_id = :bolha_id
        and ((cimavotos - baixavotos) >= 0)
        and deleted_at is null
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