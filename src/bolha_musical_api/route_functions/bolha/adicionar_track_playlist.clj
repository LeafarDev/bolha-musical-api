(ns bolha-musical-api.route-functions.bolha.adicionar-track-playlist
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clj-spotify.core :as sptfy]))

(defn- remove-cache-saved-membros
  "Pego todos os membros e removo cache saved"
  [bolha-id]
  (let [membros-bolha (query/busca-membros-bolha query/db {:bolha_id bolha-id})]
    (doseq [membro (doall membros-bolha)]
      (do
        (log/info (str "removendo cache -> " (str "saved-" (:spotify_access_token membro) "-" bolha-id)))
        (wcar* (car/del (str "saved-" (:spotify_access_token membro) "-" bolha-id)))))))

(defn adicionar-track-playlist
  "Adicionar uma nova track para playlist"
  [request track-id]
  (let [user (sat/extract-user request)
        bolha-atual (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        bolha-key (str "playlist-bolha-" (:id bolha-atual))
        votos-bolha-key (str "playlist-bolha-votos-" (:id bolha-atual))
        track (sptfy/get-a-track {:id track-id} (:spotify_access_token user))]
    (if (or (false? (:apenas_lider_adiciona_track bolha-atual))
            (and (true? (:apenas_lider_adiciona_track bolha-atual)) (= (:id user) (:user_lider_id bolha-atual))))
      (do (log/info (str "add " bolha-key))
          (query/adicionar-track-playlist
           query/db {:bolha_id         (:id bolha-atual)
                     :spotify_track_id track-id
                     :duration_ms      (:duration_ms track)
                     :current_playing  0
                     :created_at       (df/nowMysqlFormat)})
          (remove-cache-saved-membros (:id bolha-atual))
          (wcar* (car/del bolha-key))
          (wcar* (car/del votos-bolha-key))
          (ok {:message (translate (read-string (:language_code user)) :done)}))
      (precondition-failed! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                 :error)}))))