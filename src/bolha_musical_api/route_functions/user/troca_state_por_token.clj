(ns bolha-musical-api.route-functions.user.troca-state-por-token
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general-functions.user.user :as gfuser]
            [bolha-musical-api.general-functions.user.create-token :as ct]
            [bolha-musical-api.general-functions.spotify.login-codigo :as gflg]
            [bolha-musical-api.locale.dicts :refer [translate]]))

(defn get-token
  "troca o código de login, pelo token do usuário"
  [state]
  (if (gflg/state-trocavel-por-token? state)
    (let [token (not-empty (ct/criar-token-user (gfuser/get-user-by-state state)))]
      (if (true? (gflg/codigo-ja-utilizado? state))
        (ok {:token (str "Token " token)})))
    (bad-request! {:message (translate nil :invalid-state-login-code)})))

