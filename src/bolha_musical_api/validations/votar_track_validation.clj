(ns bolha-musical-api.validations.votar_track_validation
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.validations.validations :refer [metis-bool? metis-bool-or-number-bool? call-validation]]
            [metis.core :as metis]
            [bolha-musical-api.locale.dicts :refer [translate]]
            [bolha-musical-api.util :refer [string-is-keyword?]]))
; :numericality {:only-integer true :is-not-an-int ":wrong-formatting"
;                                               :less-than    2 :is-not-less-than ":between -1 1"
;                                               :greater-than -2 :is-not-greater-than ":between -1 1"}
(metis/defvalidator rule [:track_interno_id [:presence {:message ":field-required"}]]
  [:cimavoto [:presence {:message ":field-required"}]
   ['metis-bool-or-number-bool? {:message "fack u kay?"}]]
  [:refletir_spotify [:presence {:message ":field-required"}]
   ['metis-bool? {:message ":field-must-be-boolean"}]])

(defn votar-track-playlist-validate
  "passa a validação para função de validação e tradução de erros"
  [handler]
  (fn [request]
    (call-validation handler request rule)))
