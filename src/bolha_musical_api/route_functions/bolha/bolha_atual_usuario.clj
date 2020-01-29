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
      (let [membros-bolha (query/busca-membros-bolha query/db {:bolha_id (:id bolha)})
            membros-bolha-com-me (map #(conj (as-> % membro
                                                   (dissoc membro
                                                           :spotify_access_token
                                                           :spotify_current_device
                                                           :deleted_at
                                                           :checkin
                                                           :checkout
                                                           :checkout
                                                           :created_by
                                                           :created_at
                                                           :updated_at)
                                                   (if-not (:mostrar_localizacao_mapa membro)
                                                     (dissoc membro :longitude
                                                             :latitude)
                                                     membro))
                                             {:me (sptfy/get-current-users-profile
                                                   {}
                                                   (:spotify_access_token %))})
                                      membros-bolha)]
        (ok (conj bolha {:membros membros-bolha-com-me})))
      (not-found! {:message (translate (read-string (:language_code user)) :u-are-not-in-a-bubble)}))))