-- :name get-bolha-by-id
-- :command :select
-- :result :one
-- :doc Busca bolha pelo seu id
select bolhas.*,
   St_x(bolhas.referencia_raio_fixo)       as latitude,
   St_y(bolhas.referencia_raio_fixo)       as longitude,
   referencias_tamanhos_bolhas.raio_metros as raio
from   bolhas
   join referencias_tamanhos_bolhas
     on referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
where  bolhas.deleted_at is null and bolhas.id = :id
limit  0, 1;

-- :name get-bolha-atual-usuario
-- :command :select
-- :result :one
-- :doc Busca bolha que o usuário está atualmente participando
select bolhas.*,
   St_x(bolhas.referencia_raio_fixo)       as latitude,
   St_y(bolhas.referencia_raio_fixo)       as longitude,
   referencias_tamanhos_bolhas.raio_metros as raio
from   bolhas
   join bolhas_membros
     on bolhas_membros.user_id = :user_id
        and bolhas_membros.bolha_id = bolhas.id
        and bolhas_membros.deleted_at is null
        and bolhas_membros.checkout is null
   join referencias_tamanhos_bolhas
     on referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
where  bolhas.deleted_at is null
limit  0, 1;
-- :name get-bolha-by-referencia
-- :command :select
-- :result :one
-- :doc Busca bolha que o usuário está atualmente participando
select bolhas.*,
   St_x(bolhas.referencia_raio_fixo)       as latitude,
   St_y(bolhas.referencia_raio_fixo)       as longitude,
   referencias_tamanhos_bolhas.raio_metros as raio
from   bolhas
   join referencias_tamanhos_bolhas
     on referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
where  bolhas.deleted_at is null and bolhas.referencia = :referencia
limit  0, 1;
-- :name bolhas-disponiveis
-- :command :select
-- :doc Busca bolhas no alcance da localização atual do usuário
select bolhas.*,
   St_x(bolhas.referencia_raio_fixo)             as latitude,
   St_y(bolhas.referencia_raio_fixo)             as longitude,
   referencias_tamanhos_bolhas.raio_metros       as raio,
   ( 6371 * acos(cos(radians(St_x(users.ultima_localizacao))) * cos(radians(
                 St_x(bolhas.referencia_raio_fixo))) * cos(
                          radians(
                          St_y(bolhas.referencia_raio_fixo)) - radians
                          (
                 St_y(users.ultima_localizacao))) + sin(radians(
   St_x(users.ultima_localizacao)))
   * sin(
   radians(
   St_x(bolhas.referencia_raio_fixo)))) ) * 1000 as distancia_metros
from   users,
   bolhas
   join referencias_tamanhos_bolhas
     on referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
where  users.id = :user_id
having distancia_metros <= referencias_tamanhos_bolhas.raio_metros
order  by distancia_metros
limit  0, 30;
-- :name criar-bolha :insert :raw
-- :command :insert
-- :doc Insere uma bolha
INSERT INTO bolhas
            (referencia,
             apelido,
             eh_fixa,
             referencia_raio_fixo,
             tamanho_bolha_referencia_id,
             user_lider_id,
             cor,
             rocket_chat_canal_id)
VALUES      (:referencia,
             :apelido,
             :eh_fixa,
             GeomFromText(:referencia_raio_fixo),
             :tamanho_bolha_referencia_id,
             :user_lider_id,
             :cor,
             :rocket_chat_canal_id);

-- :name get-bolhas-ativas
-- :command :select
-- :doc Busca bolhas ativas com tracks e membros presentes
select bolhas.*,
   St_x(bolhas.referencia_raio_fixo)       as latitude,
   St_y(bolhas.referencia_raio_fixo)       as longitude,
   referencias_tamanhos_bolhas.raio_metros as raio
from   bolhas
   join referencias_tamanhos_bolhas
     on referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
   join bolhas_membros on bolhas_membros.bolha_id = bolhas.id and bolhas_membros.checkout is null
   join bolhas_playlists_tracks on bolhas_playlists_tracks.bolha_id = bolhas.id
where  bolhas.deleted_at is null
group by bolhas.id;

-- :name get-referencias-tamanhos-bolhas
-- :command :select
-- :doc Busca bolhas ativas com tracks e membros presentes
select referencias_tamanhos_bolhas.*
from referencias_tamanhos_bolhas
where referencias_tamanhos_bolhas.deleted_at is null
group by referencias_tamanhos_bolhas.id;
