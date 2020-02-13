(ns bolha-musical-api.route-functions.user.follow
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]
            [bolha-musical-api.util :refer [rmember]]
            [clojure.tools.logging :as log]))

(defn follow
  "Seguir usuário"
  [request]
  (let [user (sat/extract-user request)
        data (:body-params request)
        spotify_client_id (:id data)]
    (log/info (str "AEEEEEEEEEEEEEEEEEEEEE" data))
    (if-not (nil? spotify_client_id)
      (do
        (sptfy/follow-artists-or-users {:ids spotify_client_id :type "user"} (:spotify_access_token user))
        (ok {:message (translate (read-string (:language_code user)) :done)}))
      (not-found! {:message (translate (read-string (:language_code user)) :error)}))))

