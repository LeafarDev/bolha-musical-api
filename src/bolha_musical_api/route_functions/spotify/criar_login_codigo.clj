(ns bolha-musical-api.route-functions.spotify.criar-login-codigo
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.locale.dicts :refer [translate]]))

(defn criar-novo-codigo-de-login
  "Cria um uuid no banco e o retorna"
  []
  (try-let [novo-codigo-login (str (java.util.UUID/randomUUID))
            data-codigo {:id         novo-codigo-login
                         :expires_at (df/parse-mysql-date-time-format (df/agora-add-minutos 15))
                         :created_at (df/nowMysqlFormat)}
            create-result (query/criar-novo-codigo-de-login query/db data-codigo)]
           (ok data-codigo)
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate nil :session-start-error)}))))

