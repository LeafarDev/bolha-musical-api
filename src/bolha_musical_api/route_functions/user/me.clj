(ns bolha-musical-api.route-functions.user.me
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [clojure.tools.logging :as log]))

(defn me
  [request]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))]
           ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
           (ok (sptfy/get-current-users-profile {} (:spotify_access_token user)))
           (catch Exception e
             (log/error e)
             (bad-request! {:message "Não foi possivel buscar as informações com o spotify"}))))