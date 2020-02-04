(ns bolha-musical-api.validations.validations
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer :all]
            [clojure.string :as str]
            [bolha-musical-api.general-functions.spotify.access-token :as sat]))

(defn- translate-messages
  [lista language]
  (map #(if (string-is-keyword? %)
          (apply translate language (read-string (first (str/split % #" ")))
                 (rest (str/split % #" ")))
          %)
       lista))

;;(translate :en :wrong-formatting)

(defn call-validation
  "gambs para validar input do usuário como middleware"
  [handler request rule-f]
  (let [language (read-string (:language_code (sat/extract-user request)))
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

(defn metis-bool-or-number-bool? [map key _]
  (when-not (or (boolean? (get map key))
                (and (> (get map key) -2) (< (get map key) 2)))
    (str "The field must be true or false.")))

(defn metis-is-keyword? [map key _]
  (when (not-empty (get map key))
    (when-not (string-is-keyword? (get map key))
      (str "The field must be a keyword."))))