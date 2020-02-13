(ns bolha-musical-api.route-functions.bolha.votar-track-playlist
  (:require [clj-spotify.core :as sptfy]
            [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]))

(defn votar-track-playlist
  "Sair da bolha atual do usuário"
  [request]
  (let [user (sat/extract-user request)
        bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        data (:body-params request)
        agora (df/nowMysqlFormat)
        track-interna (query/get-track-by-id query/db {:id (:track_interno_id data)})
        data-remove {:deleted_at agora, :track_interno_id (:id track-interna), :user_id (:id user)}
        data-insert (-> data
                        (conj {:user_id (:id user), :created_by (:id user), :created_at agora})
                        (dissoc :refletir_spotify))
        token (:spotify_access_token user) id-param-sptfy {:ids (:spotify_track_id track-interna)}
        user-saved-bolha-key (str "liked-" (:spotify_access_token user))
        votos-bolha-key (str "playlist-bolha-votos-" (:id bolha))]
    (when-not (= (:id bolha) (:bolha_id track-interna))
      (log/info id-param-sptfy)
      (bad-request! {:message (str "hum?" (:id bolha) "/" track-interna)}))
    (query/remover-voto-track-playlist query/db data-remove)
    (wcar* (car/del votos-bolha-key))
    (when-not (= -1 (:cimavoto data))
      (log/info data-insert)
      (query/adicionar-voto-track-playlist query/db data-insert)
      (when (:refletirs_potify data)
        (wcar* (car/del user-saved-bolha-key))
        (if (:cimavoto data)
          (sptfy/save-tracks-for-user id-param-sptfy token)
          (sptfy/remove-users-saved-tracks id-param-sptfy token))))
    (ok {:message (translate (read-string (:language_code user)) :done)})))