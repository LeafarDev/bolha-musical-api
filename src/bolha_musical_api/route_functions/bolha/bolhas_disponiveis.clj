(ns bolha-musical-api.route_functions.bolha.bolhas_disponiveis
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query_defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [clojure.set :refer :all]))

(defn  bolhas-disponiveis
  "Busca bolhas no alcance da localização atual do usuário logado"
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            bolhas-disponiveis (query/bolhas-disponiveis query/db {:user_id (:id user)})]
           (ok bolhas-disponiveis)
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message "Não foi possivel buscar bolhas no momento"}))))
