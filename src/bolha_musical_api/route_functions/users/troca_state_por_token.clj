(ns bolha-musical-api.route_functions.users.troca-state-por-token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.user.user :as gfuser]
            [bolha-musical-api.general-functions.user.create-token :as ct]
            [bolha-musical-api.general_functions.spotify.login_codigo :as gflg]))

(defn get-token
  "docstring"
  [state]
  (if-let [check-state (not= false (gflg/verifica-codigo-eh-trocavel-por-token state))]
    (if-let [token (not-empty (ct/criar-token-user (gfuser/get-user-by-state state)))]
      (if (= true (gflg/checkar-codigo state))
        (ok {:token (str "Token " token)}))
      (internal-server-error! {:message "O sistema não conseguiu criar o token, tente novamente mais tarde"}))
    (bad-request! {:message "O state parece inválido, faça o login novamente"})))