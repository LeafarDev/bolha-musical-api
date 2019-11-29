(ns bolha-musical-api.routes.user
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [bolha-musical-api.route_functions.user.set-localizacao-atual :as gfsla]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [bolha-musical-api.middleware.spotify_refresh_token :refer [sptfy-refresh-tk-mw]]
            [bolha-musical-api.route_functions.user.me :refer [me]]))

(s/defschema LocalizacaoSchema
  {:latitude  s/Num
   :longitude s/Num})

(def user
  (context "/api/users" []
    :tags ["api"]
    (GET "/me" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (me request))
    (POST "/localizacao/atual" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body [localizacao LocalizacaoSchema]
      :summary "Recebo a localização atual do usuário e salvo no banco"
      (gfsla/set-localizacao-atual request))))
