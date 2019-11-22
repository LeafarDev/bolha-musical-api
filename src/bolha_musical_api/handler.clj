(ns bolha-musical-api.handler
  (:require [compojure.api.sweet :refer :all]
            [bolha-musical-api.routes.spotify :refer :all]
            [bolha-musical-api.routes.pizza :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Bolha-musical-api"
                   :description "Compojure Api example"}
            :tags [{:name "api", :description "some apis"}]}}}

   pizza
   spotify))
