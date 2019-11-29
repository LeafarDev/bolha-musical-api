-- :name busca-bolha-atual-usuario
-- :command :select
-- :result :one
-- :doc Busca bolha que o usuário está atualmente participando
select bolhas.*,
       St_x(bolhas.referencia_raio_fixo) as latitude,
       St_y(bolhas.referencia_raio_fixo) as longitude,
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