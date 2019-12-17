(ns bolha-musical-api.route-functions.bolha.sair-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [clojure.set :refer :all]
            [bolha-musical-api.query-defs :as query]))

(defn- call-internal-falha-msg-padrao
  ([]
   (internal-server-error! {:message "Não consegui sair da bolha, tente novamente mais tarde pls"}))
  ([msg]
   (internal-server-error! {:message msg})))

(defn sair-bolha
  "Sair da bolha atual do usuário"
  [request]
  (try-let [user (sat/extract-user request)
            user-id (:id user)]
           (try (query/remove-usuario-bolha query/db {:user_id (:id user) :checkout (df/nowMysqlFormat)})
                (ok {:message "Feito"})
                (catch Exception e
                  (log/error e)
                  (call-internal-falha-msg-padrao)))
           (catch Exception e
             (log/error e)
             (call-internal-falha-msg-padrao))))