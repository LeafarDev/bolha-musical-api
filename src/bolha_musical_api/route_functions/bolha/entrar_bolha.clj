(ns bolha-musical-api.route_functions.bolha.entrar-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query_defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [clojure.set :refer :all]
            [bolha-musical-api.route_functions.bolha.bolha-atual-usuario :as rfbau]
            [bolha-musical-api.query_defs :as query]))

(defn- bolha-existe?
  [bolha-id]
  (when (query/get-bolha-by-id query/db {:id bolha-id})
    true))

(defn- ja-esta-na-bolha?
  [bolha-id user-id]
  (let [bolha-atual (query/get-bolha-atual-usuario query/db {:user_id user-id})]
    (when (= bolha-id (:id bolha-atual))
      true)))

(defn- bolha-disponivel?
  [bolha-id user-id]
  (let [data (query/bolhas-disponiveis query/db {:user_id user-id})]
    (not-empty (filter #(= bolha-id (:id %)) data))))

(defn- call-internal-falha-msg-padrao
  ([]
   (internal-server-error! {:message "Não consegui inserir você na bolha, tente novamente mais tarde pls"}))
  ([msg]
   (internal-server-error! {:message msg})))

(defn entrar-bolha
  "Entra em uma bolha, se já estiver em outra, sairá dela"
  [request bolha-id]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            user-id (:id user)]
           (cond
             (not (bolha-existe? bolha-id))
             (not-found! {:message "Não encontrei essa bolha, tem certeza que ela existe?"})
             (ja-esta-na-bolha? bolha-id user-id)
             (precondition-failed {:message "Você já está nessa bolha"})
             (not (bolha-disponivel? bolha-id user-id))
             (precondition-failed {:message "Você não cumpre os requisitos para entrar nessa bolha"})
             :else
             (try (query/remove-usuario-bolha query/db {:user_id (:id user) :checkout (df/nowMysqlFormat)})
                  (query/insert-membro-bolha query/db {:bolha_id bolha-id,
                                                       :user_id  user-id
                                                       :checkin  (df/nowMysqlFormat)})
                  (rfbau/bolha-atual-usuario request)
                  (catch Exception e
                    (log/error e)
                    (call-internal-falha-msg-padrao))))
           (catch Exception e
             (log/error e)
             (call-internal-falha-msg-padrao))))


