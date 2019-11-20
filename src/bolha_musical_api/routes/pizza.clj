(ns bolha-musical-api.routes.pizza
  (:require [compojure.api.sweet :refer :all]
            [environ.core :refer [env]]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))
(def database-url
  (env :database-url))

(s/defschema PizzaSchema
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(def pizza
  (context "/api" []
    :tags ["api"]

    (GET "/plus" []
      :return {:result Long}
      :query-params [x :- Long, y :- Long]
      :summary "adds two numbers together"
      (ok {:result (+ x y)}))

    (GET "/env" []
      :return {:result String}
      :summary "Retuns env"
      (ok {:result (str database-url)}))

    (POST "/echo" []
      :return PizzaSchema
      :body [pizza PizzaSchema]
      :summary "echoes a Pizza"
      (ok pizza))))