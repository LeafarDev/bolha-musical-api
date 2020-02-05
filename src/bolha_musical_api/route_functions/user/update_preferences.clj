(ns bolha-musical-api.route-functions.user.update_preferences
  (:require [ring.util.http-response :refer :all]
            [try-let :refer [try-let]]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.tools.logging :as log]))

(defn- corrige-language-code
  [language-code]
  (if (or (= language-code ":pt-br") (= language-code :pt-br))
    ":pt-br"
    ":en"))

(defn update-preferences
  "Atualiza informações preferencias do usuário"
  [request]
  (try-let [user (sat/extract-user request)
            data (:body-params request)
            novas-preferencias (-> data
                                   (assoc :language_code (corrige-language-code (:language_code data))))
            prep-data (conj {:id (:id user)} novas-preferencias)]
           (when (= false (:tocar_track_automaticamente novas-preferencias))
             (sptfy/pause-a-users-playback {} (:spotify_access_token user)))
           (query/update-user-preferences query/db prep-data)
           (ok {:message (translate (read-string (:language_code (sat/extract-user request))) :done)})
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :error)}))))