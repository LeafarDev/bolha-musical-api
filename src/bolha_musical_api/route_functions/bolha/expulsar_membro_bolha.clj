(ns bolha-musical-api.route-functions.bolha.expulsar-membro-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.spotify.bolha :as gfbol]
            [clojure.set :refer :all]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]))

(defn expulsar-membro-bolha
  "Retirar membro da bolha"
  [request]
  (let [user (sat/extract-user request)
        bolha-antiga (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        data-user-expulso (:body-params request)]
    (if-not (nil? (:id data-user-expulso))
      (if (and (= (:user_lider_id bolha-antiga) (:id user))
               (not= (:id data-user-expulso) (:id user)))
        (do (gfbol/remover-usuario-bolha (:id bolha-antiga) (:id data-user-expulso) true)
            (ok {:message (translate (read-string (:language_code user)) :done)}))
        (forbidden! {:message (translate (read-string (:language_code user)) :cant-do-that)}))
      (not-found! {:message (translate (read-string (:language_code user)) :not-found)}))))