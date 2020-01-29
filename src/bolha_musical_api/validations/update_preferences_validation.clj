(ns bolha-musical-api.validations.update_preferences_validation
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.validations.validations :refer :all]
            [metis.core :as metis]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [string-is-keyword?]]))

(metis/defvalidator rule
  [:language_code [:presence {:message ":field-required"}]
   ['metis-is-keyword? {:message ":wrong-formatting"}]]
  [:mostrar_localizacao_mapa [:presence {:message ":field-required"}]
   ['metis-bool? {:message ":field-must-be-boolean"}]])

(defn update-preferences-validate
  "passa a validação para função de validação e tradução de erros"
  [handler]
  (fn [request]
    (call-validation handler request rule)))
