(ns bolha-musical-api.route_functions.bolha.criar-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query_defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [bolha-musical-api.route_functions.bolha.bolha-atual-usuario :as rfbau]
            [clojure.set :refer :all]))

(defn criar-bolha
  "Retorno a bolha atual do usuário, junto com seus membros (contendo localizacao e o 'me' resumido deles)"
  [request bolha]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            referencia (str (java.util.UUID/randomUUID))
            data-prep-insert (conj bolha {:referencia_raio_fixo (str "POINT(" (:latitude user) " " (:longitude user) ")")
                                          :referencia           referencia
                                          :agora                (df/nowMysqlFormat)
                                          :user_id              (:id user)})
            insert-result (query/criar-bolha-fixa query/db data-prep-insert)
            bolha-criada (query/get-bolha-by-referencia query/db {:referencia referencia})
            remove-usuario-bolhas-result (query/remove-usuario-bolha query/db {:user_id (:id user) :checkout (df/nowMysqlFormat)})
            add-usuario-bolha-criada-result (query/insert-membro-bolha query/db {:bolha_id (:id bolha-criada),
                                                                                 :user_id  (:id user)
                                                                                 :checkin (df/nowMysqlFormat)})]
           (rfbau/bolha-atual-usuario request)
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message "Não foi possivel inserir a bolha, tente novamente mais tarde"}))))