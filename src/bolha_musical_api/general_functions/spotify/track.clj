(ns bolha-musical-api.general-functions.spotify.track
  (:require [clj-http.client :as client]))

(defn get-user-top-tracks
  "Refreshes an access token using a refresh token that was generated
  via the OAuth 2 Authorization Code flow."
  [token]
  (-> "https://api.spotify.com/v1/me/top/tracks"
      (client/get {:headers {:Authorization (str "Bearer " token)}
                   :as :json})
      :body))

