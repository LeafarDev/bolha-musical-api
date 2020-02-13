(ns bolha-musical-api.route-functions.bolha.criar-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.spotify.bolha :as gfbol]
            [bolha-musical-api.general-functions.rocket-chat.rocket :as rocket]
            [bolha-musical-api.route-functions.bolha.bolha-atual-usuario :as rfbau]
            [clojure.set :refer :all]))

(defn criar-bolha
  "Retorno a bolha atual do usuário, junto com seus membros (contendo localizacao e o 'me' resumido deles)"
  [request]
  (try-let [user (sat/extract-user request)
            bolha (:body-params request)
            bolha-antiga (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
            referencia (str (java.util.UUID/randomUUID))
            rocker-chat-room-criado (rocket/criar-canal referencia user)
            data-prep-insert (conj bolha {:referencia_raio_fixo (str "POINT(" (:latitude user) " " (:longitude user) ")")
                                          :referencia           referencia
                                          :agora                (df/nowMysqlFormat)
                                          :user_lider_id        (:id user)
                                          :rocket_chat_canal_id (:_id rocker-chat-room-criado)})]
           (when-not (nil? bolha-antiga)
             (gfbol/remover-usuario-bolha (:id bolha-antiga) (:id user)))
           (query/criar-bolha query/db data-prep-insert)
           (query/insert-membro-bolha query/db {:bolha_id (:id (query/get-bolha-by-referencia query/db
                                                                                              {:referencia referencia})),
                                                :user_id  (:id user) :checkin (df/nowMysqlFormat)})

           (ok (query/get-bolha-atual-usuario query/db {:user_id (:id user)}))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :failed-to-insert-the-bubble)}))))