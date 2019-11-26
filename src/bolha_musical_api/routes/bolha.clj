(ns bolha-musical-api.routes.bolha
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))
(def bolha
  (context "/api" []
    :tags ["api"]
    (GET "/spotify/bolhas" []
      :return {:codigo java.lang.String}
      :summary "Retorna bolhas disponiveis pro usuário"
      (ok []))))