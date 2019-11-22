-- :name criar-novo-codigo-de-login
-- :command :insert
-- :result :raw
-- :doc insere uma linha em spotify_login_codes como um novo uuid() para que o usuário possa utiliza esse código pra logar com spotify
insert into spotify_login_codes (id , expires_at, created_at)
values (:id, :expires_at, :created_at);

-- :name busca-codigo-valido
-- :command :select
-- :result :one
-- :doc busca um codigo não expirado e não utilizado
select * from spotify_login_codes
where expires_at >= STR_TO_DATE(:agora, "%Y-%m-%d %H:%i:%s") and id = :id
 and checked_at is null;