(ns bolha-musical-api.general_functions.spotify.access_token
  (:require [clj-http.client :as client]
            [try-let :refer [try-let]]
            [environ.core :refer [env]]
            [bolha-musical-api.query_defs :as query]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [clojure.tools.logging :as log]))

(def spotify-auth-encoded (env :spotify-auth-encoded))
(def spotify-redirect-uri-encoded (env :spotify-redirect-uri))
(def spotify-client-id (env :spotify-client-id))
(def spotify-client-secret (env :spotify-client-secret))

(defn get-access-token-client
  "When the authorization code has been received, you will need to exchange it with an access
  token by making a POST request to the Spotify Accounts service, this time to
   its /api/token endpoint: POST "
  [code]
  (try
    (-> "https://accounts.spotify.com/api/token"
        (client/post {:form-params {:grant_type   "authorization_code"
                                    :redirect_uri spotify-redirect-uri-encoded
                                    :code         code}
                      :headers     {:Authorization spotify-auth-encoded}
                      :as          :json})
        :body)
    (catch Exception e (log/error e "There was an error in get-access-token-client"))))
(defn refresh-access-token
  "Refreshes an access token using a refresh token that was generated
  via the OAuth 2 Authorization Code flow."
  [client-id client-secret refresh_token]
  (-> "https://accounts.spotify.com/api/token"
      (client/post {:form-params {:grant_type    "refresh_token"
                                  :refresh_token refresh_token}
                    :basic-auth  [client-id client-secret]
                    :as          :json})
      :body))

(defn handle-user-spotify-refresh-token
  "Faço a lógica de chamar o refresh token e tambem atualizar os dados necessários do usuário"
  [user]
  (try-let [token_data (refresh-access-token spotify-client-id spotify-client-secret (:spotify_refresh_token user))]
           (try-let [dados_tratados_update (gfuser/converte-token-data-spotify-em-dado-local token_data)
                     juncao-trados-user-id (conj {:id (:id user)} dados_tratados_update)
                     result-update (query/update-user-spotify-refresh-token query/db juncao-trados-user-id)
                     usuario-atualizado (gfuser/get-user-by-email (:email user))]
                    usuario-atualizado
                    (catch Exception e
                      (log/error e "gfuser/converte-token-data-spotify-em-dado-local"))
                    (catch Exception e
                      (log/error e "query/update-user-spotify-refresh-token"))
                    (catch Exception e
                      (log/error e "gfuser/get-user-by-email")))))