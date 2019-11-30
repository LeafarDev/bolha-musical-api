(ns bolha-musical-api.routes.bolha
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [bolha-musical-api.middleware.spotify_refresh_token :refer [sptfy-refresh-tk-mw]]
            [bolha-musical-api.route_functions.bolha.bolha-atual-usuario :as gfbau]
            [bolha-musical-api.route_functions.bolha.bolhas_disponiveis :as gfbp]))
(def bolha
  (context "/api/spotify/bolhas" request
    :tags ["api"]
    (GET "/disponiveis" []
      :summary "Busca bolhas no alcance da localização atual do usuário logado"
      (gfbp/bolhas-disponiveis request))
    (GET "/atual" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Retorna bolhas disponiveis pro usuário"
      (gfbau/bolha-atual-usuario request))))