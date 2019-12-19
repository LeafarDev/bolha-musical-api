(ns bolha-musical-api.route-functions.bolha.adicionar-track-playlist
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.redis_defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clj-spotify.core :as sptfy]
            [clj-time.core :as t]
            [clj-time.local :as l]))

(defn adicionar-track-playlist
  "Adicionar uma nova track para playlist"
  [request track-id]
  (let [user (sat/extract-user request)
        bolha-atual (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        bolha-key (str "playlist-bolha-" (:id bolha-atual))
        track (sptfy/get-a-track {:id track-id} (:spotify_access_token user))]
    (try (do
           (query/adicionar-track-playlist
            query/db {:bolha_id         (:id bolha-atual)
                      :spotify_track_id track-id
                      :duration_ms      (:duration_ms track)
                      :current_playing  0
                      :created_at       (df/nowMysqlFormat)})
           (wcar* (car/del bolha-key)))
         (ok {:message (translate (:language_code user) :done)})
         (catch Exception e
           (log/error e)
           (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                        :failed-to-create-track)})))))
