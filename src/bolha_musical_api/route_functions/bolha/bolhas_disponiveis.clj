(ns bolha-musical-api.route-functions.bolha.bolhas-disponiveis
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]))

(defn  bolhas-disponiveis
  "Busca bolhas no alcance da localização atual do usuário logado"
  [request]
  (try-let [user (sat/extract-user request)
            bolhas-disponiveis (query/bolhas-disponiveis query/db {:user_id (:id user)})]
           (ok bolhas-disponiveis)
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :cant-fetch-bubbles)}))))
