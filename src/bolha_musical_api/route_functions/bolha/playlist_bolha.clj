(ns bolha-musical-api.route-functions.bolha.playlist-bolha
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.query-defs :as query]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [rmember]]
            [bolha-musical-api.redis_defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [bolha-musical-api.general-functions.spotify.track :refer [relacionar-tracks-local-com-spotify
                                                                       relacionar-tracks-playlist-user-saved
                                                                       votos-tracks-playlist]]))
(defn- prepare-playlist
  [bolha-id spotify-access-token language]
  (if-let [playlist (not-empty (rmember (str "playlist-bolha-" bolha-id) 3600 #(relacionar-tracks-local-com-spotify bolha-id spotify-access-token)))]
    (let [user-saved-tracks (relacionar-tracks-playlist-user-saved playlist spotify-access-token)
          votos (rmember (str "playlist-bolha-votos-" bolha-id) 3600 #(votos-tracks-playlist playlist))]
      (map conj playlist user-saved-tracks votos))
    (not-found! {:message (translate language :there-is-no-music)})))

(defn playlist-bolha
  "retorno a playlist atual da bolha usuário"
  [request]
  (let [user (sat/extract-user request)
        language (read-string (:language_code user))
        bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        spotify-access-token (:spotify_access_token user)
        bolha-id (:id bolha)]
    (if (not-empty bolha)
      (ok (prepare-playlist bolha-id spotify-access-token language))
      (not-found! {:message (translate (read-string (:language_code user)) :u-are-not-in-a-bubble)}))))

