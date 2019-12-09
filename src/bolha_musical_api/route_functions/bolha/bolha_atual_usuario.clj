(ns bolha-musical-api.route-functions.bolha.bolha-atual-usuario
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [clojure.set :refer :all]))

(defn bolha-atual-usuario
  "Retorno a bolha atual do usuário, junto com seus membros (contendo localizacao e o 'me' resumido deles)"
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})]
           (if (not-empty bolha)
             (let [membros-bolha (not-empty (query/busca-membros-bolha query/db {:bolha_id (:id bolha)}))
                   membros-bolha-com-me (map #(conj % {:me (sptfy/get-current-users-profile {} (:spotify_access_token user))}) membros-bolha)]
               (ok (conj bolha {:membros membros-bolha-com-me})))
             (not-found! {:message "Usuário não está em uma bolha"}))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message "Não foi possivel buscar a bolha do usuário, tente novamente mais tarde"}))))