(ns bolha-musical-api.general-functions.spotify.bolha
  (:require [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.rocket-chat.rocket :as rocket]))

(defn remover-usuario-bolha
  "Remove usuário da bolha e de seu chat"
  ([bolha-id user-id] (remover-usuario-bolha bolha-id user-id false))
  ([bolha-id user-id expulso]
   (let [bolha (query/get-bolha-by-id query/db {:id bolha-id}) user (query/get-user-by-id query/db {:id user-id})
         membros (query/busca-membros-bolha query/db {:bolha_id bolha-id})]
     (rocket/remover-usuario-canal (:rocket_chat_canal_id bolha) (:rocket_chat_id user))
     (query/remove-usuario-bolha query/db {:checkout (df/nowMysqlFormat),
                                           :user_id  user-id
                                           :foi_expulso  expulso})
     (when (= 1 (count membros))
       (query/remover-bolha query/db {:id    (:id bolha)
                                      :agora (df/nowMysqlFormat)})))))

(defn adicionar-usuario-bolha
  "Adiciona o usuário na bolha e no chat"
  [bolha-id user-id]
  (let [bolha (query/get-bolha-by-id query/db {:id bolha-id}) user (query/get-user-by-id query/db {:id user-id})]
    (rocket/adicionar-usuario-canal (:rocket_chat_canal_id bolha) (:rocket_chat_id user))
    (query/insert-membro-bolha query/db {:checkin (df/nowMysqlFormat), :user_id user-id, :bolha_id bolha-id})))