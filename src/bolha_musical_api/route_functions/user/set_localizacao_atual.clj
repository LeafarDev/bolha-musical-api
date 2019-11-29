(ns bolha-musical-api.route_functions.user.set-localizacao-atual
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query_defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.user.user :as gfuser]))

(defn set-localizacao-atual
  "Recebo latitude e longitude e salvo no usuário"
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            body (:body-params request)
            data (conj {:point (str "POINT(" (:latitude body) " " (:longitude body) ")")} {:agora (df/nowMysqlFormat)} (select-keys user [:id]))
            result (query/update-user-localizacao-atual query/db data)]
           (ok {:message "Atualizado com sucesso"})
           (catch Exception e
             (log/error e)
             (bad-request! {:message "Não foi possivel atualizar a localização ,tente novamente mais tarde"}))))
