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
            [bolha-musical-api.validations.votar_track_validation :refer [votar-track-playlist-validate]]
            [bolha-musical-api.route-functions.bolha.criar-bolha :as rfcbol]
            [bolha-musical-api.route-functions.bolha.sair-bolha :as rfsbol]
            [bolha-musical-api.route-functions.bolha.entrar-bolha :as rfebol]
            [metis.core :as metis]
            [bolha-musical-api.validations.criar_bolha_validation :refer [criar-bolha-validate]]
            [bolha-musical-api.route-functions.bolha.bolhas-disponiveis :as rfbp]
            [bolha-musical-api.route-functions.bolha.bolha-referencias-tamanhos :as rfgref]
            [bolha-musical-api.route-functions.bolha.adicionar-track-playlist :as rfatp]
            [bolha-musical-api.route-functions.bolha.votar-track-playlist :as rfvot]
            [bolha-musical-api.route-functions.bolha.current-playing :as rfcp]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]))

(def bolha
  (context "/api/v1/spotify/bolhas" request
    :tags ["api"]
    (GET "/referencias" []
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Busca as opções de tamanhos de bolha"
      (rfgref/bolha-referencias-tamanhos request))
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
    (GET "/playlist/current-playing" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Retorna bolhas disponiveis pro usuário"
      (rfcp/current-playing request))
    (POST "/playlist/track" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body-params [id :- String]
      :summary "Insere uma track em uma bolha"
      (rfatp/adicionar-track-playlist request id))
    (POST "/playlist/track/votar" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw votar-track-playlist-validate]
      :summary "Insere uma track em uma bolha"
      (rfvot/votar-track-playlist request))
    (POST "/" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw criar-bolha-validate]
      :summary "Recebo informações da bolha para criar"
      (rfcbol/criar-bolha request))
    (POST "/entrar" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :body-params [id :- Integer]
      :summary "Insere um usuário em uma bolha"
      (rfebol/entrar-bolha request id))
    (POST "/sair" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Remove usuário da bolha"
      (rfsbol/sair-bolha request))))