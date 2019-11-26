(ns bolha-musical-api.routes.spotify
  (:require [compojure.api.sweet :refer :all]
            [bolha-musical-api.route_functions.spotify.criar-login-codigo :as rfclc]
            [bolha-musical-api.route_functions.users.spotify-callback :as usercriacao]
            [bolha-musical-api.route_functions.users.troca-state-por-token :as rfutspt]
            [bolha-musical-api.middleware.spotify_refresh_token :refer [sptfy-refresh-tk-mw]]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [schema.core :as s]))

(s/defschema SpotifyCallBackSchema
  {:code  s/Str
   :state s/Str})

(def spotify
  (context "/api" []
    :tags ["api"]
    (GET "/spotify/login/codigo/novo" []
      ; :return {:id java.lang.String :expires_at java.lang.String :created_at String}
      :summary "Retorna um código para o client pode logar no spotify"
      (rfclc/criar-novo-codigo-de-login))
    (GET "/spotify/login/callback" []
      :query-params [code :- String, state :- String]
      :summary "Recebe o callback do spotify e retorna o usuário se tudo estiver certo"
      (usercriacao/tratar-usuario-spotify-callback code state))
    (GET "/spotify/state/trocar/token" []
      :query-params [state]
      ; :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (rfutspt/get-token state))
    (GET "/spotify/refresh/teste" []
      :middleware [sptfy-refresh-tk-mw]
      (ok (str "check u privileges")))))