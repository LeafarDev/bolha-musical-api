(ns bolha-musical-api.route-functions.bolha.bolha-atual-usuario
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.set :refer :all]))

(defn bolha-atual-usuario
  "Retorno a bolha atual do usuário, junto com seus membros (contendo localizacao e o 'me' resumido deles)"
  [request]
  (let [user (sat/extract-user request)
        bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})]
    (if (not-empty bolha)
      (let [membros-bolha (not-empty (query/busca-membros-bolha query/db {:bolha_id (:id bolha)}))
            membros-bolha-com-me (map #(conj % {:me (sptfy/get-current-users-profile {} (:spotify_access_token %))}) membros-bolha)]
        (ok (conj bolha {:membros membros-bolha-com-me})))
      (not-found! {:message (translate (read-string (:language_code user)) :u-are-not-in-a-bubble)}))))