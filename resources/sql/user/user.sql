-- :name get-user-by-email
-- :command :select
-- :result :one
-- :doc Busca usuário pelo seu email
select * from users
where  email = :email;

-- :name get-user-by-state
-- :command :select
-- :result :one
-- :doc Busca usuário pelo seu state
select * from users
where  spotify_last_state = :state;

-- :name insert-user-spotify-callback :! :n
-- :command :insert
-- :doc Insere um novo usuário a partir dos dados vindos do callback do spotify
insert into users (email , spotify_client_id, spotify_access_token, spotify_scope, spotify_token_expires_at, spotify_refresh_token, spotify_last_state)
values (:email , :spotify_client_id, :spotify_access_token, :spotify_scope, :spotify_token_expires_at, :spotify_refresh_token, :spotify_last_state);

-- :name update-user-spotify-callback :! :n
-- :command :update
-- :doc Atualiza um usuário a partir dos dados vindos do callback do spotify
update users
set email                    = :email,
    spotify_client_id        = :spotify_client_id,
    spotify_access_token     = :spotify_access_token,
    spotify_scope            = :spotify_scope,
    spotify_token_expires_at = :spotify_token_expires_at,
    spotify_refresh_token    = :spotify_refresh_token,
    spotify_last_state       = :spotify_last_state
where id = :id;

-- :name update-user-spotify-refresh-token :! :n
-- :command :update
-- :doc Atualiza um usuário a partir dos dados vindos do callback do spotify
update users
set spotify_access_token     = :spotify_access_token,
    spotify_scope            = :spotify_scope,
    spotify_token_expires_at = :spotify_token_expires_at
where id = :id;
