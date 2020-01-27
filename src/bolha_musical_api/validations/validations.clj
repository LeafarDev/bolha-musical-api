(ns bolha-musical-api.validations.validations
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [string-is-keyword?]]
            [clojure.tools.reader.reader-types :as r]
            [clojure.string :as str]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]
            [clojure.tools.logging :as log]))

(defn- translate-messages
  [lista language]
  (map #(if (string-is-keyword? %)
          (apply translate language (read-string (first (str/split % #" ")))
                 (rest (str/split % #" ")))
          %)
       lista))

(defn call-validation
  "gambs para validar input do usuário como middleware"
  [handler request rule-f]
  (let [language (:language_code (sat/extract-user request))
        result-validate (rule-f (:body-params request))]
    (if (empty? result-validate)
      (handler request)
      (unprocessable-entity!
       (map (fn* [rule-error]
                 (zipmap [(first rule-error)]
                         [(translate-messages (second rule-error) language)]))
            (map vector (keys result-validate) (vals result-validate)))))))

(defn metis-bool? [map key _]
  (when-not (boolean? (get map key))
    (str "The field must be true or false.")))