(ns bolha-musical-api.route-functions.user.update_current_device
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [bolha-musical-api.query-defs :as query]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.set :refer :all]))

(defn update-current-device
  "Atualizo e retorno lista de devices do usuário"
  [request]
  (let [user (sat/extract-user request) data (:body-params request)]
    (query/update-user-spotify-current-device query/db {:spotify_current_device (:device_id data) :id (:id user)})
    (sptfy/transfer-current-users-playback {:device_ids [(:device_id data)]} (:spotify_access_token user))
    (wcar* (car/del (str "get-current-users-available-devices-" (:id user))))
    (ok {:message (translate (read-string (:language_code user)) :done)})))

