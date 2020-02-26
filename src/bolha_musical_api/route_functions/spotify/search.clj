(ns bolha-musical-api.route-functions.spotify.search
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.spotify.track :as gftrack]
            [bolha-musical-api.util :refer [rmember]]
            [bolha-musical-api.locale.dicts :refer [translate]]))

(defn search
  "Busca por uma track, se o usuário não informar nada na pesquisa retorna as top tracks do usuário"
  [request query]
  (let [user (sat/extract-user request)]
    ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
    (if (not-empty query)
      (ok (:tracks (rmember (str "search-" query) 3600 #(sptfy/search {:q query :type "track" :market "BR" :limit 50 :offset 0} (:spotify_access_token user)))))
      (ok (rmember (str "get-user-top-tracks-" (:id user)) 7200 #(gftrack/get-user-top-tracks (:spotify_access_token user)))))))
