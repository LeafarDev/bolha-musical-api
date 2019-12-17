(ns bolha-musical-api.route-functions.bolha.entrar-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.route-functions.bolha.bolha-atual-usuario :as rfbau]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]))

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

(defn entrar-bolha
  "Entra em uma bolha, se já estiver em outra, sairá dela"
  [request bolha-id]
  (try-let [user (sat/extract-user request)
            user-id (:id user)]
           (cond
             (not (bolha-existe? bolha-id))
             (not-found! {:message (translate (:language_code user) :bubble-not-found)})
             (ja-esta-na-bolha? bolha-id user-id)
             (precondition-failed! {:message (translate (:language_code user) :already-in-this-bubble)})
             (not (bolha-disponivel? bolha-id user-id))
             (precondition-failed! {:message (translate (:language_code user) :bubble-not-available)})
             :else
             (try (query/remove-usuario-bolha query/db {:user_id (:id user) :checkout (df/nowMysqlFormat)})
                  (query/insert-membro-bolha query/db {:bolha_id bolha-id,
                                                       :user_id  user-id
                                                       :checkin  (df/nowMysqlFormat)})
                  (rfbau/bolha-atual-usuario request)
                  (catch Exception e
                    (log/error e)
                    (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                                 :failed-to-get-in-the-bubble)}))))))


