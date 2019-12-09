(ns bolha-musical-api.general-functions.spotify.access-token
  (:require [clj-http.client :as client]
            [try-let :refer [try-let]]
            [environ.core :refer [env]]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [buddy.sign.jwt :as jwt]
            [clojure.tools.logging :as log]))

(defn get-access-token-client
  "When the authorization code has been received, you will need to exchange it with an access
  token by making a POST request to the Spotify Accounts service, this time to
   its /api/token endpoint: POST "
  [code]
  (try
    (-> "https://accounts.spotify.com/api/token"
        (client/post {:form-params {:grant_type   "authorization_code"
                                    :redirect_uri (env :spotify-redirect-uri)
                                    :code         code}
                      :headers     {:Authorization (env :spotify-auth-encoded)}
                      :as          :json})
        :body)
    (catch Exception e (log/error e "There was an error in get-access-token-client"))))

(defn extract-pure-token
  [request]
  (get-in request [:headers "authorization"]))

(defn extract-token
  [request]
  (clojure.string/replace (extract-pure-token request) #"Token " ""))

(defn extract-token-data
  [token]
  (jwt/unsign token (env :auth-key) {:alg :hs512}))

(defn refresh-access-token
  "Refreshes an access token using a refresh token that was generated
  via the OAuth 2 Authorization Code flow."
  [refresh_token]
  (-> "https://accounts.spotify.com/api/token"
      (client/post {:form-params {:grant_type    "refresh_token"
                                  :refresh_token refresh_token}
                    :basic-auth  [(env :spotify-client-id) (env :spotify-client-secret)]
                    :as          :json})
      :body))

(defn handle-user-spotify-refresh-token
  "Faço a lógica de chamar o refresh token e tambem atualizar os dados necessários do usuário"
  [user]
  (try-let [token_data (refresh-access-token (:spotify_refresh_token user))
            dados_tratados_update (gfuser/converte-token-data-spotify-em-dado-local token_data)
            juncao-trados-user-id (conj {:id (:id user)} dados_tratados_update)
            result-update (query/update-user-spotify-refresh-token query/db juncao-trados-user-id)
            usuario-atualizado (gfuser/get-user-by-email (:email user))]
           true
           (catch Exception e
             (log/error e "refresh-access-token")
             false)
           (catch Exception e
             (log/error e "gfuser/converte-token-data-spotify-em-dado-local")
             false)
           (catch Exception e
             (log/error e "query/update-user-spotify-refresh-token")
             false)
           (catch Exception e
             (log/error e "gfuser/get-user-by-email")
             false)))