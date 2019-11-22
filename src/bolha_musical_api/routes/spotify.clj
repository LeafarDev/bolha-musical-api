(ns bolha-musical-api.routes.spotify
  (:require [compojure.api.sweet :refer :all]
            [bolha-musical-api.route_functions.spotify.login_codigo :as lc]
            [bolha-musical-api.route_functions.users.spotify-callback :as usercriacao]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [schema.core :as s]))

(s/defschema SpotifyCallBackSchema
  {:code  s/Str
   :state s/Str})
(defn logme
  "docstring"
  [code state]
  (print (str code state))
  {:code  code
   :state state})

(def spotify
  (context "/api" []
    :tags ["api"]
    (GET "/spotify/login/codigo/novo" []
      :return {:codigo java.lang.String}
      :summary "Retorna um código para o client pode logar no spotify"
      (ok {:codigo (lc/criar-novo-codigo-de-login)}))
    (GET "/spotify/login/callback" []
      :query-params [code :- String, state :- String]
      :summary "Recebe o callback do spotify e retorna o usuário se tudo estiver certo"
      (usercriacao/tratar-usuario-spotify-callback code state))
    (GET "/spotify/teste/token" []
      :middleware [token-auth-mw cors-mw authenticated-mw]
      (ok (str "Parece que seu token ta suave")))))