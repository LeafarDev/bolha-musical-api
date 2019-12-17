(ns bolha-musical-api.route-functions.user.set-localizacao-atual
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.user.user :as gfuser]))

(defn set-localizacao-atual
  "Recebo latitude e longitude e salvo no usuário"
  [request]
  (try-let [user (sat/extract-user request)
            body (:body-params request)
            data (conj {:point (str "POINT(" (:latitude body) " " (:longitude body) ")") :agora (df/nowMysqlFormat)} (select-keys user [:id]))
            result (query/update-user-localizacao-atual query/db data)]
           (ok {:message (translate (:language_code user) :location-updated)})
           (catch Exception e
             (log/error e)
             (bad-request! {:message (translate (:language_code (sat/extract-user request))
                                                      :cant-update-location)}))))