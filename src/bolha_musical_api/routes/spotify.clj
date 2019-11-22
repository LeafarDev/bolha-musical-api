(ns bolha-musical-api.routes.spotify
  (:require [compojure.api.sweet :refer :all]
            [bolha-musical-api.route_functions.spotify.login_codigo :as lc]
            [bolha-musical-api.route_functions.users.user_criacao :as usercriacao]
            [ring.util.http-response :refer :all]
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
      (ok (usercriacao/criar-usuario-spotify-callback code state)))))