(ns bolha-musical-api.route-functions.user.me
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.tools.logging :as log]))

(defn me
  "Retorna do spotify as informações do usuário"
  [request]
  (try-let [user (sat/extract-user request)]
           ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
           (ok (sptfy/get-current-users-profile {} (:spotify_access_token user)))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                          :spotify-not-responding)}))))