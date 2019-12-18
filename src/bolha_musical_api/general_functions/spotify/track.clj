(ns bolha-musical-api.general-functions.spotify.track
  (:require [clj-http.client :as client]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [clojure.set :refer :all]
            [clojure.tools.logging :as log]))

(defn get-user-top-tracks
  "top tracks do usuário"
  [token]
  (-> "https://api.spotify.com/v1/me/top/tracks"
      (client/get {:headers {:Authorization (str "Bearer " token)}
                   :as :json})
      :body))

(defn relacionar-tracks-local-com-spotify
  [bolha-id spotify-access-token]
  (if-let [tracks-bancos (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id bolha-id}))]
    (let [ids-string (clojure.string/join "," (map :spotify_track_id (doall tracks-bancos)))
          tracks-spotify (sptfy/get-several-tracks {:ids ids-string} spotify-access-token)
          tracks-bancos-resumidas (map #(select-keys % [:spotify_track_id
                                                        :started_at
                                                        :current_playing
                                                        :cimavotos
                                                        :baixavotos
                                                        :bolha_id])
                                       (doall tracks-bancos))]
      (map conj (:tracks tracks-spotify) tracks-bancos-resumidas))))