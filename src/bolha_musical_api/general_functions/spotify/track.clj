(ns bolha-musical-api.general-functions.spotify.track
  (:require [clj-http.client :as client]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.util :refer [partition-by-max-sized-piece]]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [bolha-musical-api.util :refer [rmember]]
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
    (let [ids (map :spotify_track_id (doall tracks-bancos))
          tracks-spotify (get-several-tracks ids spotify-access-token)
          tracks-bancos-resumidas (map #(-> %
                                            (rename-keys {:id :id_interno})
                                            (select-keys [:id_interno
                                                          :started_at
                                                          :current_playing
                                                          :bolha_id]))
                                       (doall tracks-bancos))]
      (map conj tracks-spotify tracks-bancos-resumidas))))

(defn check-users-saved-tracks
  [tracks-ids spotify-access-token bolha-id]
  (let [pedacos-tracks (partition-by-max-sized-piece 50 tracks-ids)
        relacao-curtidas (rmember (str "saved-" spotify-access-token "-" bolha-id) 60
                                  #(map (fn* [pedaco] (sptfy/check-users-saved-tracks {:ids (clojure.string/join "," pedaco)} spotify-access-token)) pedacos-tracks))]
    (map #(zipmap [:saved] [%]) (reduce concat relacao-curtidas))))

(defn relacionar-tracks-playlist-user-saved
  [tracks-playlist token bolha-id]
  (let [ids (map :id (doall tracks-playlist))]
    (check-users-saved-tracks ids token bolha-id)))

(defn votos-tracks-playlist
  [tracks-playlist]
  (map #(zipmap [:votos] [(rmember (str "track-bolha-votos-" (:id_interno %))
                                   10
                                   (query/get-votos-track query/db {:track_interno_id (:id_interno %)}))])
       tracks-playlist))