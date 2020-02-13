-- :name busca-membros-bolha
-- :command :select
-- :doc Busca bolha que o usuário está atualmente participando
SELECT bolhas_membros.*,
       ST_X(users.ultima_localizacao) as latitude ,
       ST_Y(users.ultima_localizacao) as longitude,
       users.spotify_access_token,
       users.spotify_current_device,
       users.data_ultima_localizacao,
       users.mostrar_localizacao_mapa,
       users.tocar_track_automaticamente
FROM   bolhas_membros
       JOIN bolhas
         ON bolhas.id = bolhas_membros.bolha_id
            AND bolhas.deleted_at IS NULL
       JOIN users
         ON users.id = bolhas_membros.user_id
            AND users.deleted_at IS NULL
WHERE  bolhas_membros.bolha_id = :bolha_id
       AND bolhas_membros.deleted_at IS NULL
       AND bolhas_membros.checkout IS NULL

-- :name insert-membro-bolha :! :n
-- :command :insert
-- :doc Insiro um usuário como membro em uma bolha
insert into bolhas_membros (bolha_id ,  user_id, checkin)
values (:bolha_id ,  :user_id, :checkin);

-- :name remove-usuario-bolha :! :n
-- :command :insert
-- :doc Tiro o usuário de qualquer bolha que esteja
update bolhas_membros
set checkout = :checkout,
    foi_expulso = :foi_expulso
where user_id = :user_id and checkout is null

-- :name qtd-membros-ativos-bolha
-- :command :select
-- :result :one
-- :doc Quantidade de membros ativo da bolha
SELECT count(*) as qtd
FROM   bolhas_membros
       JOIN bolhas
         ON bolhas.id = bolhas_membros.bolha_id
            AND bolhas.deleted_at IS NULL
       JOIN users
         ON users.id = bolhas_membros.user_id
            AND users.deleted_at IS NULL
WHERE  bolhas_membros.bolha_id = :bolha_id
       AND bolhas_membros.deleted_at IS NULL
       AND bolhas_membros.checkout IS NULL

-- :name busca-membros-fora-range-bolha
-- :command :select
-- :doc Busca bolha que o usuário está fora de range
SELECT bolhas_membros.*,
       St_x(users.ultima_localizacao)                  AS latitude,
       St_y(users.ultima_localizacao)                  AS longitude,
       referencias_tamanhos_bolhas.raio_metros         AS raio,
       ( 6371 * Acos(Cos(Radians(St_x(users.ultima_localizacao))) * Cos(Radians(
                     St_x(bolhas.referencia_raio_fixo))) * Cos(
                              Radians(
         St_y(bolhas.referencia_raio_fixo)) - Radians
         (
         St_y(users.ultima_localizacao))) + Sin(Radians(
         St_x(users.ultima_localizacao))) * Sin(
         Radians(
         St_x(bolhas.referencia_raio_fixo)))) ) * 1000 AS distancia_metros,
       users.spotify_access_token,
       users.spotify_current_device,
       users.data_ultima_localizacao,
       users.mostrar_localizacao_mapa,
       users.tocar_track_automaticamente
FROM   users
       JOIN bolhas
         ON bolhas.deleted_at IS NULL
       JOIN bolhas_membros
         ON bolhas_membros.bolha_id = bolhas.id
            AND bolhas_membros.checkout IS NULL
            AND bolhas_membros.deleted_at IS NULL
            AND bolhas_membros.user_id = users.id
       JOIN referencias_tamanhos_bolhas
         ON referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
WHERE  users.deleted_at IS NULL
HAVING distancia_metros > ( referencias_tamanhos_bolhas.raio_metros
                            + 20 )
ORDER  BY distancia_metros

-- :name busca-membros-inativos-bolha
-- :command :select
-- :doc Busca bolha que o usuário está atualmente inativo
SELECT bolhas_membros.*,
       St_x(users.ultima_localizacao)                  AS latitude,
       St_y(users.ultima_localizacao)                  AS longitude,
       referencias_tamanhos_bolhas.raio_metros         AS raio,
       ( 6371 * Acos(Cos(Radians(St_x(users.ultima_localizacao))) * Cos(Radians(
                     St_x(bolhas.referencia_raio_fixo))) * Cos(
                              Radians(
         St_y(bolhas.referencia_raio_fixo)) - Radians
         (
         St_y(users.ultima_localizacao))) + Sin(Radians(
         St_x(users.ultima_localizacao))) * Sin(
         Radians(
         St_x(bolhas.referencia_raio_fixo)))) ) * 1000 AS distancia_metros,
       users.spotify_access_token,
       users.spotify_current_device,
       users.data_ultima_localizacao,
       users.mostrar_localizacao_mapa,
       users.tocar_track_automaticamente,
       TIMESTAMPDIFF(MINUTE , data_ultima_localizacao, now()) AS ultima_acao
FROM   users
       JOIN bolhas
         ON bolhas.deleted_at IS NULL
       JOIN bolhas_membros
         ON bolhas_membros.bolha_id = bolhas.id
            AND bolhas_membros.checkout IS NULL
            AND bolhas_membros.deleted_at IS NULL
            AND bolhas_membros.user_id = users.id
       JOIN referencias_tamanhos_bolhas
         ON referencias_tamanhos_bolhas.id = bolhas.tamanho_bolha_referencia_id
WHERE  users.deleted_at IS NULL
HAVING ultima_acao > 50
