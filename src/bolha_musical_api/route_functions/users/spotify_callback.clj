(ns bolha-musical-api.route_functions.users.spotify-callback
  (:require [clj-spotify.core :as sptfy]
            [bolha-musical-api.route_functions.spotify.access_token :as sat]
            [bolha-musical-api.route_functions.spotify.login_codigo :as loginCode]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [environ.core :refer [env]]
            [clojure.set :refer :all]))

(defn tratar-usuario-spotify-callback
  "Recebo parametros do callback do spotify e crio o usuário"
  [code, state]
  (if (not= false (loginCode/vericar-codigo-state-eh-valido state))
    (if-let [token_data (not-empty (sat/get-access-token-client code))]
      (let [user_me (not-empty (sptfy/get-current-users-profile {} (str (token_data :access_token))))
            user_local (not-empty (gfuser/get-user-by-email (user_me :email)))]
        (if-let [dados_tratados_update (not-empty (gfuser/junta-dados-spotify user_me token_data user_local))]
          (ok {:token (str "Token " (gfuser/atualizar-usuario-callback dados_tratados_update))})
          (internal-server-error {:message "Falha ao concluir esta ação"}))
        (if-let [dados_tratados_create (not-empty (gfuser/junta-dados-spotify user_me token_data))]
          (ok {:token (str "Token " (gfuser/criar-usuario-callback dados_tratados_create))})
          (internal-server-error {:message "Falha ao concluir esta ação"})))
      (bad-request! {:message "Essa requisição parece incorreta, verifique as informações e tente novamente"}))
    false))

