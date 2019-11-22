(ns bolha-musical-api.middleware.token-auth
  (:require
   [buddy.auth.middleware :refer [wrap-authentication]]
   [bolha-musical-api.auth-resources.token-auth-backend :refer [token-backend]]))

(defn token-auth-mw
  "Middleware used on routes requiring token authentication"
  [handler]
  (wrap-authentication handler token-backend))
