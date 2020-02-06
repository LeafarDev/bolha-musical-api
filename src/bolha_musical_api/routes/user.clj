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
            [bolha-musical-api.route-functions.user.devices :as rfdevs]
            [bolha-musical-api.route-functions.user.update_current_device :as rfupdatedev]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.route-functions.user.update_preferences :as rfuppref]
            [bolha-musical-api.validations.update_preferences_validation :refer [update-preferences-validate]]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]))

(defn- validate-set-localizacao
  "gambs para validar input do usuário como middleware"
  [handler]
  (fn [request]
    (let [language (read-string (:language_code (sat/extract-user request)))
          result-validate ((metis/defvalidator validate-set-localizacao
                             [:latitude :presence {:message (translate (:language language) :enter-latitude)}]
                             [:longitude :presence {:message (translate (:language language) :enter-longitude)}])
                           (:body-params request))]
      (if (empty? result-validate)
        (handler request)
        (unprocessable-entity! result-validate)))))

(defn- validate-update-current-device
  "gambs para validar input do usuário como middleware"
  [handler]
  (fn [request]
    (let [language (read-string (:language_code (sat/extract-user request)))
          result-validate ((metis/defvalidator validate-set-localizacao [:device_id :presence {:message (translate (:language language) :enter-device)}])
                           (:body-params request))]
      (if (empty? result-validate)
        (handler request)
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
      (rfsla/set-localizacao-atual request))
    (GET "/devices" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (rfdevs/devices request))
    (GET "/following/contains" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw]
      (rfdevs/devices request))
    (PUT "/devices" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw validate-update-current-device]
      (rfupdatedev/update-current-device request))
    (PUT "/preferences" request
      :middleware [token-auth-mw cors-mw authenticated-mw sptfy-refresh-tk-mw update-preferences-validate]
      (rfuppref/update-preferences request))))