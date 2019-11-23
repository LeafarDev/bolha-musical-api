(ns bolha-musical-api.route_functions.users.me
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [bolha-musical-api.general-functions.user.create-token :as ct]
            [bolha-musical-api.general_functions.spotify.login_codigo :as gflg]))



(defn me
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))]
    (ok (sptfy/get-current-users-profile {} (:spotify_access_token user)))))