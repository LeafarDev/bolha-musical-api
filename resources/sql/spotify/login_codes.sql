-- :name criar-novo-codigo-de-login :! :n
-- :command :insert
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

-- :name busca-codigo-trocavel-por-token
-- :command :select
-- :result :one
-- :doc busca um codigo não expirado e não utilizado e com token disponivek
select spotify_login_codes.*
from spotify_login_codes
         inner join users on users.spotify_last_state = spotify_login_codes.id
where spotify_login_codes.id = :id
  and expires_at >= STR_TO_DATE(:agora, "%Y-%m-%d %H:%i:%s") and spotify_login_codes.id = :id
  and checked_at is null

-- :name update-codigo-checado :! :n
-- :command :update
-- :doc Marca o codigo como já utilizado
update spotify_login_codes
set checked_at = :agora
where id = :id;