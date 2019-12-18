(ns bolha-musical-api.route-functions.bolha.playlist-bolha
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.set :refer :all]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [rmember]]
            [bolha-musical-api.general-functions.spotify.track :refer [relacionar-tracks-local-com-spotify]]))

(defn playlist-bolha
  "retorno a playlist atual da bolha usuário"
  [request]
  (let [user (sat/extract-user request)
        bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})
        bolha-key (str "playlist-bolha-" (:id bolha))
        spotify-access-token (:spotify_access_token user)
        bolha-id (:id bolha)]
    (if (not-empty bolha)
      (if-let [playlist (not-empty (rmember bolha-key 3600 `(relacionar-tracks-local-com-spotify ~bolha-id ~spotify-access-token)))]
        (ok playlist)
        (not-found! {:message (translate (:language_code user) :there-is-no-music)}))
      (not-found! {:message (translate (:language_code user) :u-are-not-in-a-bubble)}))))