(ns bolha-musical-api.route-functions.user.devices
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.util :refer [rmember]]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]
            [clojure.tools.logging :as log]))

(defn devices
  "Retorno lista de devices do usuário"
  [request]
  (let [user (sat/extract-user request)
        devices-result (rmember (str "get-current-users-available-devices-" (:id user))
                                5
                                #(sptfy/get-current-users-available-devices {} (:spotify_access_token user)))]
    (when (empty? (:devices devices-result))
      (query/update-user-spotify-current-device query/db {:spotify_current_device nil, :id (:id user)}))
    (ok (:devices devices-result))))