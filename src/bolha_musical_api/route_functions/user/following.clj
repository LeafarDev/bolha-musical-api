(ns bolha-musical-api.route-functions.user.following
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]
            [bolha-musical-api.util :refer [rmember]]))

(defn following
  "Verifica se o usuário está seguindo outro usuário"
  [request]
  (let [user (sat/extract-user request)
        data (:params request)
        spotify_client_id (:id data)
        result (rmember (str (:id user) "-is-following-" spotify_client_id "?") 60
                        #(sptfy/user-following-artists-or-users? {:type "user" :ids spotify_client_id} (:spotify_access_token user)))]
    (if-not (nil? spotify_client_id)
      (ok {:seguindo (first result)})
      (not-found! {:message (translate (read-string (:language_code user)) :not-found)}))))

