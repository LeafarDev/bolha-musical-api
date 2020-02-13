(ns bolha-musical-api.route-functions.bolha.bolha-referencias-tamanhos
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]))

(defn bolha-referencias-tamanhos
  "Retorna referencias de bolhas do banco"
  [request]
  (try-let [referencias (query/get-referencias-tamanhos-bolhas query/db {})]
           (ok referencias)
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :error)}))))
