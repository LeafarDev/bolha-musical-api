(ns bolha-musical-api.general-functions.user.create-token
  (:require
   [environ.core :refer [env]]
   [buddy.sign.jwt :as jwt]))

(defn criar-token-user
  "Create a signed json web token. The token contents are; id, spotify_client_id, email
   and token expiration time. Tokens are valid for 1 week."
  [user]
  (let [stringify-user (-> user
                           (update :id str)
                           (update :spotify_client_id str)
                           (update :email str)
                           (assoc :exp (.plusSeconds (java.time.Instant/now) 604800)))
        token-contents (select-keys stringify-user [:id :spotify_client_id :email :exp])]
    (jwt/sign token-contents (env :auth-key) {:alg :hs512})))