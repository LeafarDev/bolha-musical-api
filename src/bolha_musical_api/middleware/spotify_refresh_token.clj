(ns bolha-musical-api.middleware.spotify-refresh-token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [try-let :refer [try-let]]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [clojure.tools.logging :as log]))

(defn- callRefresh
  [user]
  (when (sat/handle-user-spotify-refresh-token user)
    true))

(defn- falta-cinco-minutos-ou-menos?
  [spotify-token-expires-at]
  (>= 5 (df/intervalo-minutos (l/local-now) spotify-token-expires-at)))

(defn- ja-expirou?
  [spotify-token-expires-at]
  (df/date-greater? (l/local-now) spotify-token-expires-at))

(defn sptfy-refresh-tk-mw
  "Pego o usuário apartir do token, e verifico se é preciso utilizar refresh
  token do spotify, no caso se apartir de 55 minutos passado após criação do token, já utilizo o refresh)"
  [handler]
  (fn [request]
    (try-let [user (sat/extract-user request)
              spotify-token-expires-at (c/from-sql-date (:spotify_token_expires_at user))]
             (if-not (ja-expirou? spotify-token-expires-at)
               (if (falta-cinco-minutos-ou-menos? spotify-token-expires-at)
                 ;;; Essa função chama o refresh no spotify e atualiza usuário com o novo token
                 (if (callRefresh user)
                   ;;; Se deu tudo certo ao chamar o reflesh no spotify, continuo normalmente
                   (handler request)
                   (internal-server-error! {:message "Impossivel processar o token"}))
                 ;;; se houver mais de cinco minutos pra expirar, continuo normalmente
                 (handler request))
               ;;; Caso o token já tenha expirado totalmente
               (if (callRefresh user)
                 (handler request)
                 (internal-server-error! {:message "Impossivel processar o token"})))
             (catch Exception e
               (log/error e)
               (bad-request! {:message "Impossivel processar o token"})))))


