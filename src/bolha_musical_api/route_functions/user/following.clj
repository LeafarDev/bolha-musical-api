(ns bolha-musical-api.route-functions.user.following
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]))

(defn  following
  "Verifica se o usuário está seguindo outro usuário"
  [request]
  (let [user (sat/extract-user request)
        spotify_client_id (:spotify_client_id (:body-params request))
        result (sptfy/user-following-artists-or-users? {:ids spotify_client_id} (:spotify_access_token user))]
    (ok (:seguindo (first result)))))