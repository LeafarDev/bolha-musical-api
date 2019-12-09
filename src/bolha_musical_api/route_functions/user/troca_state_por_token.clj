(ns bolha-musical-api.route-functions.user.troca-state-por-token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [bolha-musical-api.general-functions.user.create-token :as ct]
            [bolha-musical-api.general-functions.spotify.login-codigo :as gflg]))

(defn get-token
  "docstring"
  [state]
  (if (gflg/state-trocavel-por-token? state)
    (if-let [token (not-empty (ct/criar-token-user (gfuser/get-user-by-state state)))]
      (if (true? (gflg/codigo-ja-utilizado? state))
        (ok {:token (str "Token " token)}))
      (internal-server-error! {:message "O sistema não conseguiu criar o token, tente novamente mais tarde"}))
    (bad-request! {:message "O state parece inválido, faça o login novamente"})))