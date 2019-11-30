(ns bolha-musical-api.route_functions.user.spotify-callback
  (:require [clj-spotify.core :as sptfy]
            [bolha-musical-api.general_functions.spotify.access_token :as sat]
            [bolha-musical-api.general_functions.spotify.login_codigo :as gflg]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [environ.core :refer [env]]
            [clojure.set :refer :all]))

(defn- junta-dados-state
  [state dados]
  (conj {:spotify_last_state state} dados))

(defn tratar-usuario-spotify-callback
  "Recebo parametros do callback do spotify e crio o usuário"
  [code, state]
  (if-not (false? (gflg/state-valido-em-callback? state))
    (if-let [token-data (not-empty (sat/get-access-token-client code))]
      (if-let [user-me (not-empty (sptfy/get-current-users-profile {} (str (token-data :access_token))))]
        (if-let [user-local (not-empty (gfuser/get-user-by-email (user-me :email)))]
          (if-let [dados-tratados-update (not-empty (gfuser/junta-dados-spotify user-me token-data user-local))]
            (ok {:token (str "Token " (gfuser/atualiza-usuario-banco-callback (junta-dados-state state dados-tratados-update)))})
            (internal-server-error {:message "Falha ao concluir esta ação"}))
          (if-let [dados-tratados-create (not-empty (gfuser/junta-dados-spotify user-me token-data))]
            (ok {:token (str "Token " (gfuser/cria-usuario-banco-callback (junta-dados-state state dados-tratados-create)))})
            (internal-server-error {:message "Falha ao concluir esta ação"}))))
      (bad-request! {:message "Essa requisição parece incorreta, verifique as informações e tente novamente"}))
    (bad-request! {:message "Codigo de login inválido, atualize e tente novamente"})))

