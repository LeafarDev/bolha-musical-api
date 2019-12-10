(ns bolha-musical-api.handler
  (:require [compojure.api.sweet :refer :all]
            [bolha-musical-api.routes.spotify :refer :all]
            [bolha-musical-api.routes.user :refer :all]
            [bolha-musical-api.routes.bolha :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.tools.logging :as log]))
(timbre/set-level! :info)
(timbre/merge-config! {:level :info})
;;; blacklist: ["io.pedestal.*" "org.eclipse.jetty.*"]
(def app
  (->
   (api
    {:swagger
     {:ui   "/"
      :spec "/swagger.json"
      :data {:info {:title       "Bolha-musical-api"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}
    user
    spotify
    bolha)))

