(ns bolha-musical-api.locale.dicts
  (:require [tongue.core :as tongue]))

(def dicts
  {:en              {:enter-latitude         "Enter latitude"
                     :enter-longitude        "Enter longitude"
                     :spotify-not-responding "Spotify is not responding. Please try again later"
                     :cant-update-location "I couldn't update your location. Please try again later"
                     :location-updated "Location successfully updated"}
   :pt-br           {:enter-latitude         "Informe a latitude"
                     :enter-longitude        "Informe a longitude"
                     :spotify-not-responding "Spotify não está respondendo. Por favor tente novamente mais tarde"
                     :cant-update-location "Não consegui atualizar sua localização. Por favor tente novamente mais tarde"
                     :location-updated "Localização atualizada com sucesso"}
   :tongue/fallback :pt-br})

(def translate                                              ;; [locale key & args] => string
  (tongue/build-translate dicts))