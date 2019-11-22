(ns bolha-musical-api.middleware.spotify_refresh_token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [buddy.sign.jwt :as jwt]))

(defn- callRefresh
  [user handler request]
  (if (not= false sat/handle-user-spotify-refresh-token user)
    (handler request)
    (internal-server-error {:message "Impossivel atualizar token do usuário"})))
(defn sptfy-refresh-tk-mw
  "Pego o usuário apartir do token, e verifico se é preciso utilizar refresh
  token do spotify, no caso se apartir de 55 minutos passado após criação do token, já utilizo o refresh)"
  [handler]
  (fn [request]
    (let [token (get-in request [:headers "authorization"])
          token-sem-prefixo (clojure.string/replace token #"Token " "")
          unsigned (jwt/unsign token-sem-prefixo (env :auth-key) {:alg :hs512})
          user (gfuser/get-user-by-email (:email unsigned))
          spotify_token_expires_at (c/from-sql-date (:spotify_token_expires_at user))
          ja-expirou? (df/date-greater? (l/local-now) spotify_token_expires_at)]
      (if (not= true ja-expirou?)
        (if-let [falta-cinco-minutos-ou-menos? (<= 5 (df/intervalo-minutos (l/local-now) spotify_token_expires_at))]
          (callRefresh user handler request)
          (handler request))
        (callRefresh user handler request)))))


