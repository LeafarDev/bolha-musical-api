(ns bolha-musical-api.route-functions.bolha.entrar-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.route-functions.bolha.bolha-atual-usuario :as rfbau]
            [bolha-musical-api.general-functions.spotify.bolha :as gfbol]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]))

(defn- bolha-existe?
  [nova-bolha-id]
  (when (query/get-bolha-by-id query/db {:id nova-bolha-id})
    true))

(defn- ja-esta-na-bolha?
  [nova-bolha-id user-id]
  (let [bolha-atual (query/get-bolha-atual-usuario query/db {:user_id user-id})]
    (when (= nova-bolha-id (:id bolha-atual))
      true)))

(defn- bolha-disponivel?
  [nova-bolha-id user-id]
  (let [data (query/bolhas-disponiveis query/db {:user_id user-id})]
    (not-empty (filter #(= nova-bolha-id (:id %)) data))))

(defn entrar-bolha
  "Entra em uma bolha, se já estiver em outra, sairá dela"
  [request nova-bolha-id]
  (try-let [user (sat/extract-user request)
            user-id (:id user)
            rocket-chat-user-id (:rocket_chat_id user)
            bolha-antiga (query/get-bolha-atual-usuario query/db {:user_id (:id user)})]
           (cond
             (not (bolha-existe? nova-bolha-id))
             (not-found! {:message (translate (read-string (:language_code user)) :bubble-not-found)})
             (ja-esta-na-bolha? nova-bolha-id user-id)
             (precondition-failed! {:message (translate (read-string (:language_code user)) :already-in-this-bubble)})
             (not (bolha-disponivel? nova-bolha-id user-id))
             (precondition-failed! {:message (translate (read-string (:language_code user)) :bubble-not-available)})
             :else
             (try (when-not (nil? bolha-antiga)
                    (gfbol/remover-usuario-bolha (:id bolha-antiga) user-id))
                  (gfbol/adicionar-usuario-bolha nova-bolha-id user-id)
                  (rfbau/bolha-atual-usuario request)
                  (catch Exception e
                    (log/error e)
                    (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                                 :failed-to-get-in-the-bubble)}))))))


