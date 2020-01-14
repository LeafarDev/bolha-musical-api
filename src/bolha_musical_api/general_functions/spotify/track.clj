(ns bolha-musical-api.general-functions.spotify.track
  (:require [clj-http.client :as client]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.redis_defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.set :refer :all]
            [clojure.tools.logging :as log]))

(defn get-user-top-tracks
  "top tracks do usuário"
  [token]
  (-> "https://api.spotify.com/v1/me/top/tracks"
      (client/get {:headers {:Authorization (str "Bearer " token)}
                   :as      :json})
      :body))

(defn cachear-tracks
  [tracks]
  (doseq [track tracks]
    (do (print (str "| cacheando " (:id track)))
        (wcar* (car/set (:id track) track)))))

(defn get-several-tracks
  [tracks-ids token]
  (let [ids-sem-cache (filter (fn* [ids] (zero? (wcar* (car/exists ids)))) tracks-ids)
        ids-com-cache (filter (fn* [ids] (= 1 (wcar* (car/exists ids)))) tracks-ids)
        tracks-do-cache (map (fn* [ids] (wcar* (car/get ids))) ids-com-cache)
        tracks-do-spotify (sptfy/get-several-tracks {:ids (clojure.string/join "," ids-sem-cache)} token)
        todas-tracks (concat tracks-do-cache (:tracks tracks-do-spotify))]
    (cachear-tracks (:tracks tracks-do-spotify))
    (remove nil? todas-tracks)))

(defn relacionar-tracks-local-com-spotify
  [bolha-id spotify-access-token]
  (if-let [tracks-bancos (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id bolha-id}))]
    (let [tracks-spotify (get-several-tracks (map :spotify_track_id (doall tracks-bancos)) spotify-access-token)
          tracks-bancos-resumidas (map #(select-keys % [:spotify_track_id
                                                        :started_at
                                                        :current_playing
                                                        :bolha_id])
                                       (doall tracks-bancos))]
      (map conj tracks-spotify tracks-bancos-resumidas))))