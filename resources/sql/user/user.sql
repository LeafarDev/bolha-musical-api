-- :name get-user-by-email
-- :command :select
-- :result :one
-- :doc Busca usuário pelo seu email
select * from users
where  email = :email;