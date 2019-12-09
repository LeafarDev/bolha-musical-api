(ns bolha-musical-api.route-functions.spotify.search
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [bolha-musical-api.general-functions.spotify.track :as gftrack]
            [clojure.tools.logging :as log]))

(defn search
  [request query]
  (try-let [token-data (sat/extract-token-data (sat/extract-token request))
            user (gfuser/get-user-by-email (:email token-data))]
           ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
           (if (not-empty query)
             (ok (:tracks (sptfy/search {:q query :type "track" :market "BR" :limit 10 :offset 0} (:spotify_access_token user))))
             (ok (gftrack/get-user-top-tracks (:spotify_access_token user))))
           (catch Exception e
             (log/error e)
             (bad-request! {:message "Não foi possivel buscar as informações com o spotify"}))))
