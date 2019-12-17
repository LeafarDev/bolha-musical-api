(ns bolha-musical-api.route-functions.bolha.sair-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]))

(defn sair-bolha
  "Sair da bolha atual do usuário"
  [request]
  (let [user (sat/extract-user request)]
    (try (query/remove-usuario-bolha query/db {:user_id (:id user) :checkout (df/nowMysqlFormat)})
         (ok {:message (translate (:language_code user) :done)})
         (catch Exception e
           (log/error e)
           (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                        :fail-to-get-out-the-bubble)})))))