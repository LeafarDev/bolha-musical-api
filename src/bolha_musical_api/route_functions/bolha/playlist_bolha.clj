(ns bolha-musical-api.route-functions.bolha.playlist-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [clojure.set :refer :all]
            [bolha-musical-api.query-defs :as query]
            [clj-spotify.core :as sptfy]))

(defn playlist-bolha
  "retorno a playlist atual da bolha usuário"
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})]
           (if (not-empty bolha)
             (if-let [tracks-bancos (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id (:id bolha)}))]
               (let [ids-string (clojure.string/join "," (map :spotify_track_id (doall tracks-bancos)))
                     tracks-spotify (sptfy/get-several-tracks {:ids ids-string} (:spotify_access_token user))
                     tracks-bancos-resumidas (map #(select-keys % [:spotify_track_id
                                                                   :started_at
                                                                   :current_playing
                                                                   :cimavotos
                                                                   :baixavotos
                                                                   :bolha_id])
                                                  (doall tracks-bancos))]
                 (ok (map #(conj %1 %2) (:tracks tracks-spotify) tracks-bancos-resumidas)))
               (not-found! {:message "Não há nenhuma música"}))
             (not-found! {:message "Você não está em uma bolha"}))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message "Não foi possivel buscar a playlist do usuário, tente novamente mais tarde"}))))
