(ns bolha-musical-api.route-functions.user.me
  (:require [ring.util.http-response :refer :all]
            [clj-spotify.core :as sptfy]
            [try-let :refer [try-let]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [clojure.tools.logging :as log]
            [bolha-musical-api.general-functions.rocket-chat.rocket :as rocket]
            [bolha-musical-api.util :refer [rmember]]))

(defn me
  "Retorna do spotify as informações do usuário"
  [request]
  (try-let [user (sat/extract-user request)
            id-user (:id user)
            language_code (read-string (:language_code user))
            me-key (str "me-" id-user)
            token (:spotify_access_token user)
            user-data (-> user
                          (select-keys [:language_code :mostrar_localizacao_mapa :tocar_track_automaticamente]))
            user-me (rmember me-key 3600 #(sptfy/get-current-users-profile {} token))
            rocket-token (rocket/user-token user)]
           ; TODO validar a resposta do spotify, não dá exception na call mesmo dando 401
           (ok (conj user-data (assoc user-me :rocket_chat_auth_token rocket-token
                                              :user_id id-user)))
           (catch Exception e
             (log/error e)
             (internal-server-error! {:message (translate (read-string (:language_code (sat/extract-user request)))
                                                          :spotify-not-responding)}))))