(ns bolha-musical-api.route-functions.user.me
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.util :refer [rmember]]))

(defn me
  "Retorna do spotify as informações do usuário"
  [request]
  (try-let [user (sat/extract-user request)
            id-user (:id user)
            me-key (str "me-" id-user)
            token (:spotify_access_token user)]
           ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
           (ok (rmember me-key 3600 `(sptfy/get-current-users-profile {} ~token)))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (:language_code (sat/extract-user request))
                                                          :spotify-not-responding)}))))