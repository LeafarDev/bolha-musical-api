(ns bolha-musical-api.routes.bolha
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [bolha-musical-api.route-functions.bolha.playlist-bolha :as rfplaylist]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [bolha-musical-api.middleware.spotify-refresh-token :refer [sptfy-refresh-tk-mw]]
            [bolha-musical-api.route-functions.bolha.bolha-atual-usuario :as rfbau]
            [bolha-musical-api.route-functions.bolha.criar-bolha :as rfcbol]
            [bolha-musical-api.route-functions.bolha.sair-bolha :as rfsbol]
            [bolha-musical-api.route-functions.bolha.entrar-bolha :as rfebol]
            [bolha-musical-api.route-functions.bolha.bolhas-disponiveis :as rfbp]
            [bolha-musical-api.route-functions.bolha.adicionar-track-playlist :as rfatp]))

(s/defschema BolhaSchema {:apelido #"^[A-Za-záàâãéèêíïóôõöúçñÁÀÂÃÉÈÍÏÓÔÕÖÚÇÑ\s]{1,50}$"})

(def bolha
  (context "/api/v1/spotify/bolhas" request
    :tags ["api"]
    (GET "/disponiveis" []
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Busca bolhas no alcance da localização atual do usuário logado"
      (rfbp/bolhas-disponiveis request))
    (GET "/atual" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Retorna bolhas disponiveis pro usuário"
      (rfbau/bolha-atual-usuario request))
    (GET "/playlist" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Retorna bolhas disponiveis pro usuário"
      (rfplaylist/playlist-bolha request))
    (POST "/playlist/track" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body-params [id :- String]
      :summary "Insere uma track em uma bolha"
      (rfatp/adicionar-track-playlist request id))
    (POST "/" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body [bolha BolhaSchema]
      :summary "Recebo o apelido da bolha para cria-la"
      (rfcbol/criar-bolha request bolha))
    (POST "/entrar" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body-params [id :- Integer]
      :summary "Insere um usuário em uma bolha"
      (rfebol/entrar-bolha request id))
    (POST "/sair" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Remove usuário da bolha"
      (rfsbol/sair-bolha request))))