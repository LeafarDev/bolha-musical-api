(ns bolha-musical-api.route-functions.user.set-localizacao-atual
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.locale.dicts :refer [translate]]))

(defn set-localizacao-atual
  "Recebo latitude e longitude e salvo no usuário"
  [request]
  (try-let [user (sat/extract-user request)
            body (:body-params request)
            bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
            string-point (str "POINT(" (:latitude body) " " (:longitude body) ")")
            data (conj {:agora (df/nowMysqlFormat) :point string-point} (select-keys user [:id]))]
           (do (when-not (nil? bolha)
                 (when (and (= (:user_lider_id bolha) (:id user)) (false? (:eh_fixa bolha)))
                   (query/update-bolha-localizacao-atual query/db {:id                   (:id bolha)
                                                                   :referencia_raio_fixo string-point})))
               (query/update-user-localizacao-atual query/db data)
               (ok {:message (translate (read-string (:language_code user)) :location-updated)}))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :cant-update-location)}))))