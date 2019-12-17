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
            [metis.core :as metis]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]))

(defn- validate-set-localizacao
  "gambs para validar input do usuário como middleware"
  [handler]
  (fn [request]
    (let [language (:language_code (sat/extract-user request))
          result-validate ((metis/defvalidator validate-set-localizacao
                             [:latitude :presence {:message (translate (:language language) :enter-latitude)}]
                             [:longitude :presence {:message (translate (:language language) :enter-longitude)}])
                           (:body-params request))]
      (if (empty? result-validate)
        (rfsla/set-localizacao-atual request)
        (unprocessable-entity! result-validate)))))

(def user
  (context "/api/v1/users" []
    :tags ["api"]
    (GET "/me" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (me request))
    (POST "/localizacao/atual" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw validate-set-localizacao]
      :summary "Recebo a localização atual do usuário e salvo no banco"
      (rfsla/set-localizacao-atual request))))