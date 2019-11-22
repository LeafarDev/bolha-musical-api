(ns bolha-musical-api.route_functions.spotify.access_token
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]))
(def spotify-auth-encoded (env :spotify-auth-encoded))
(def spotify-redirect-uri-encoded (env :spotify-redirect-uri))

(defn get-access-token-client
  "When the authorization code has been received, you will need to exchange it with an access
  token by making a POST request to the Spotify Accounts service, this time to
   its /api/token endpoint: POST "
  [code]
  (-> "https://accounts.spotify.com/api/token"
      (client/post {:form-params {:grant_type "authorization_code"
                                  :redirect_uri spotify-redirect-uri-encoded
                                  :code       code}
                    :headers {:Authorization spotify-auth-encoded}
                    :as          :json})
      :body))
