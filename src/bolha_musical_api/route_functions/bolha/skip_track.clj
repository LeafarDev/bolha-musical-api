(ns bolha-musical-api.route-functions.bolha.skip-track
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.query-defs :as query]
            [clojure.set :refer :all]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.spotify.track :refer [sincronizar-tempo-tracks
                                                                       atualmente-tocando
                                                                       proxima
                                                                       tocar-proxima-track]]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.query-defs :as query]))

(defn skip-track
  [request]
  (let [user (sat/extract-user request)
        bolha (query/get-bolha-atual-usuario query/db {:user_id (:id user)})]
    (when (empty? bolha)
      (not-found! {:message (translate (read-string (:language_code user)) :not-found)}))
    (let [bolha-id (:id bolha)
          playlist (query/get-tracks-by-bolha-id query/db {:bolha_id bolha-id})]
      (if (and (= (:user_lider_id bolha) (:id user)) (> (count playlist) 1))
        (if-let [sincronizadas (sincronizar-tempo-tracks playlist)]
          (if-let [atualmente-tocando (not-empty (atualmente-tocando sincronizadas))]
            (do (tocar-proxima-track atualmente-tocando sincronizadas bolha-id)
                (ok {:message (translate (read-string (:language_code user)) :done)}))
            (internal-server-error! {:message (translate (read-string (:language_code user)) :error)}))
          (not-found! {:message (translate (read-string (:language_code user)) :not-found)}))
        (forbidden! {:message (translate (read-string (:language_code user)) :cant-do-that)})))))