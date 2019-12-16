(ns bolha-musical-api.routes.user
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clojure.java.io :as io]
            [bolha-musical-api.route-functions.user.set-localizacao-atual :as rfsla]
            [bolha-musical-api.middleware.token-auth :refer [token-auth-mw]]
            [bolha-musical-api.middleware.cors :refer [cors-mw]]
            [bolha-musical-api.middleware.authenticated :refer [authenticated-mw]]
            [bolha-musical-api.middleware.spotify-refresh-token :refer [sptfy-refresh-tk-mw]]
            [bolha-musical-api.route-functions.user.me :refer [me]]
            [metis.core :as metis]))

(metis/defvalidator user-validator
                    [:latitude :presence {:message (tru "Enter latitude")}]
                    [:longitude :presence {:message (tru "Enter latitude")}])


(def user
  (context "/api/v1/users" []
    :tags ["api"]
    (GET "/me" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (me request))
    (POST "/localizacao/atual" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      :summary "Recebo a localização atual do usuário e salvo no banco"
      (if-let [validation (not-empty (user-validator (:body-params request)))]
        (unprocessable-entity! validation)
        (rfsla/set-localizacao-atual request)))))