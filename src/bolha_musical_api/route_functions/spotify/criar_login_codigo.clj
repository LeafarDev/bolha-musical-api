(ns bolha-musical-api.route_functions.spotify.criar-login-codigo
  (:require [ring.util.http-response :refer :all]
            [bolha-musical-api.general_functions.spotify.login_codigo :as gflg]))

(defn criar-novo-codigo-de-login
  "Chama a criação de código e retorna pra o cliente"
  []
  (ok {:state gflg/criar-novo-codigo-de-login}))