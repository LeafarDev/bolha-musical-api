-- :name busca-membros-bolha
-- :command :select
-- :doc Busca bolha que o usuário está atualmente participando
SELECT bolhas_membros.*,
       ST_X(users.ultima_localizacao) as latitude ,
       ST_Y(users.ultima_localizacao) as longitude
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