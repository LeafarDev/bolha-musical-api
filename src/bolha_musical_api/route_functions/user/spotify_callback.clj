(ns bolha-musical-api.route-functions.user.spotify-callback
  (:require [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [bolha-musical-api.general-functions.spotify.login-codigo :as gflg]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [environ.core :refer [env]]
            [clojure.set :refer :all]
            [bolha-musical-api.locale.dicts :refer [translate]]))

(defn- junta-dados-state
  [state dados]
  (conj {:spotify_last_state state} dados))

(defn tratar-usuario-spotify-callback
  "Recebo parametros do callback do spotify e crio o usuário"
  [request]
  (let [code (:code (:params request))
        state (:state (:params request))]
    (if-not (false? (gflg/state-valido-em-callback? state))
      (let [token-data (not-empty (sat/get-access-token-client code))
            user-me (not-empty (sptfy/get-current-users-profile {} (str (token-data :access_token))))]
        (if-let [user-local (not-empty (gfuser/get-user-by-email (user-me :email)))]
          (let [dados-tratados-update (not-empty (gfuser/junta-dados-spotify user-me token-data user-local))]
            (ok {:token (str "Token " (gfuser/atualiza-usuario-banco-callback (junta-dados-state state dados-tratados-update)))}))
          (let [dados-tratados-create (not-empty (gfuser/junta-dados-spotify user-me token-data))]
            (ok {:token (str "Token " (gfuser/cria-usuario-banco-callback (junta-dados-state state dados-tratados-create)))}))))
      (bad-request! {:message (translate nil :invalid-login-code)}))))


