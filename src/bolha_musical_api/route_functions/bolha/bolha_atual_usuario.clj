(ns bolha-musical-api.route_functions.bolha.bolha-atual-usuario
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query_defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [clojure.set :refer :all]))

(defn bolha-atual-usuario
  "Retorno a bolha atual do usuário, junto com seus membros (contendo localizacao e o 'me' resumido deles)"
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            bolha (not-empty (query/busca-bolha-atual-usuario query/db {:user_id (:id user)}))
            membros-bolha (not-empty (query/busca-membros-bolha query/db {:bolha_id (:id bolha)}))
            membros-bolha-com-me (map #(conj % {:me (sptfy/get-current-users-profile {} (:spotify_access_token user))}) membros-bolha)]
           (ok (conj bolha {:membros membros-bolha-com-me}))
           (catch Exception e
             (log/error e)
             (bad-request! {:message "Não foi possivel atualizar a localização ,tente novamente mais tarde"}))))
