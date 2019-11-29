(ns bolha-musical-api.middleware.spotify_refresh_token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [try-let :refer [try-let]]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [clojure.tools.logging :as log]))

(defn- callRefresh
  [user handler request]
  (if (not= false sat/handle-user-spotify-refresh-token user)
    (internal-server-error (sat/handle-user-spotify-refresh-token user))
    (internal-server-error {:message "Impossivel atualizar token do usuário"})))

(defn- falta-cinco-minutos-ou-menos?
  [spotify_token_expires_at]
  (>= 5 (df/intervalo-minutos (l/local-now) spotify_token_expires_at)))

(defn- ja-expirou?
  [spotify_token_expires_at]
  (df/date-greater? (l/local-now) spotify_token_expires_at))

(defn sptfy-refresh-tk-mw
  "Pego o usuário apartir do token, e verifico se é preciso utilizar refresh
  token do spotify, no caso se apartir de 55 minutos passado após criação do token, já utilizo o refresh)"
  [handler]
  (fn [request]
    (try-let [token-data (sat/extract-token-data (sat/extract-token request))
              user (gfuser/get-user-by-email (:email token-data))
              spotify_token_expires_at (c/from-sql-date (:spotify_token_expires_at user))]
             (if-not (ja-expirou? spotify_token_expires_at)
               (if (falta-cinco-minutos-ou-menos? spotify_token_expires_at)
                 (callRefresh user handler request)
                 (handler request))
               (callRefresh user handler request))
             (catch Exception e
               (log/error e)
               (bad-request! {:message "Impossivel processar o token"})))))


