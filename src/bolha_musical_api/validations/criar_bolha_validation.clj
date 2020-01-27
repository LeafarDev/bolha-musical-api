(ns bolha-musical-api.validations.criar_bolha_validation
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.validations.validations :refer [metis-bool? call-validation]]
            [metis.core :as metis]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [string-is-keyword?]]))
;;{:message ":between 0 1"}
(metis/defvalidator rule
  [:eh_fixa [:presence {:message ":field-required"}
             :numericality {:only-integer true :is-not-an-int ":wrong-formatting"
                            :less-than 2 :is-not-less-than ":between 0 1"
                            :greater-than 0 :is-not-greater-than ":between 0 1"}]]

  [:apelido [:presence {:message ":field-required"}
             :length {:less-than 50 :is-not-less-than ":is-not-greater-than 50"}
             :formatted {:pattern #"^[A-Za-záàâãéèêíïóôõöúçñÁÀÂÃÉÈÍÏÓÔÕÖÚÇÑ\s]{1,50}$" :message ":wrong-formatting"}]]
  [:tamanho_bolha_referencia_id [:presence {:message ":field-required"}
                                 :numericality {:only-integer true :is-not-an-int ":wrong-formatting"}]]
  [:cor [:presence {:message ":field-required"}]])

(defn criar-bolha-validate
  "passa a validação para função de validação de criação de bolhas e tradução de erros"
  [handler]
  (fn [request]
    (call-validation handler request rule)))
