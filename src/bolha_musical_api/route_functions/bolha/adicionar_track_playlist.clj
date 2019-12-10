(ns bolha-musical-api.route-functions.bolha.adicionar-track-playlist
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.user.user :as gfuser]
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
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))
            bolha-atual (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
            track (sptfy/get-a-track {:id track-id} (:spotify_access_token user))]
           (try (query/adicionar-track-playlist
                 query/db {:bolha_id         (:id bolha-atual)
                           :spotify_track_id track-id
                           :duration_ms      (:duration_ms track)
                           :current_playing  0
                           :created_at       (df/nowMysqlFormat)})
                (ok {:message "Música adicionada com sucesso :)"})
                (catch Exception e
                  (log/error e)
                  (call-internal-falha-msg-padrao)))
           (catch Exception e
             (log/error e)
             (call-internal-falha-msg-padrao))))