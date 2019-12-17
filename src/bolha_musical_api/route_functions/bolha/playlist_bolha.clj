(ns bolha-musical-api.route-functions.bolha.playlist-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clj-spotify.core :as sptfy]))

(defn playlist-bolha
  "retorno a playlist atual da bolha usuário"
  [request]
  (let [user (sat/extract-user request)
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
          (ok (map conj (:tracks tracks-spotify) tracks-bancos-resumidas)))
        (not-found! {:message (translate (:language_code user) :there-is-no-music)}))
      (not-found! {:message (translate (:language_code user) :u-are-not-in-a-bubble)}))))
