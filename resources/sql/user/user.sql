-- :name get-user-by-email
-- :command :select
-- :result :one
-- :doc Busca usuário pelo seu email
select * from users
where  email = :email;

-- :name insert-user-spotify :! :n
-- :command :insert
-- :doc Insere um novo usuário a partir dos dados vindos do callback do spotify
insert into users (email , spotify_client_id, spotify_access_token, spotify_scope, spotify_token_expires_at, spotify_refresh_token)
values (:email , :spotify_client_id, :spotify_access_token, :spotify_scope, :spotify_token_expires_at, :spotify_refresh_token);

-- :name update-user-spotify :! :n
-- :command :update
-- :doc Atualiza um usuário a partir dos dados vindos do callback do spotify
update users
set email                    = :email,
    spotify_client_id        = :spotify_client_id,
    spotify_access_token     = :spotify_access_token,
    spotify_scope            = :spotify_scope,
    spotify_token_expires_at = :spotify_token_expires_at,
    spotify_refresh_token    = :spotify_refresh_token
where id = :id;
