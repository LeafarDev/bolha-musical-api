(ns bolha-musical-api.route-functions.bolha.adicionar-track-playlist
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.query-defs :as query]
            [clj-spotify.core :as sptfy]
            [clj-time.core :as t]
            [clj-time.local :as l]))

(defn- call-internal-falha-msg-padrao
  ([]
   (internal-server-error! {:message "Não consegui inserir essa track, tente novamente mais tarde pls"}))
  ([msg]
   (internal-server-error! {:message msg})))

(defn adicionar-track-playlist
  "Adicionar uma nova track para playlist"
  [request track-id]
  (let [user (sat/extract-user request)
        bolha-atual (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        track (sptfy/get-a-track {:id track-id} (:spotify_access_token user))]
    (try (query/adicionar-track-playlist
          query/db {:bolha_id         (:id bolha-atual)
                    :spotify_track_id track-id
                    :duration_ms      (:duration_ms track)
                    :current_playing  0
                    :created_at       (df/nowMysqlFormat)})
         (ok {:message (translate (:language_code user) :done)})
         (catch Exception e
           (log/error e)
           (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                        :failed-to-create-track)})))))
