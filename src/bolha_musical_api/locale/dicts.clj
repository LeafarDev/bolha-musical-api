(ns bolha-musical-api.locale.dicts
  (:require [tongue.core :as tongue]))

(def dicts
  {:en              {:enter-latitude               "Enter latitude"
                     :enter-longitude              "Enter longitude"
                     :spotify-not-responding       "Spotify is not responding. Please try again later"
                     :cant-update-location         "I couldn't update your location. Please try again later"
                     :location-updated             "Location successfully updated"
                     :could-not-complete-action    "Could not complete the action. Please try again later"
                     :incorrect-action             "uh?"
                     :invalid-login-code           "The login code is invalid. Try signing again"
                     :invalid-state-login-code     "The login expired or invalid. Try signing again"
                     :session-start-error          "Could not start user session. Please try again later"
                     :failed-to-get-out-the-bubble "I couldn't get out of the bubble. Please try again later"
                     :done                         "Done !"
                     :there-is-no-music            "There is no music yet"
                     :u-are-not-in-a-bubble        "You are not in a bubble"
                     :couldnt-get-playlist         "Could not get your playlist. Please try again later"
                     :bubble-not-found             "Could not find the bubble. Are you sure that this bubble exists?"
                     :already-in-this-bubble       "You are already in this room"
                     :bubble-not-available         "You do not meet the requirements to enter this bubble"
                     :failed-to-get-in-the-bubble  "Failed to enter the bubble. Please try again later"
                     :failed-to-insert-the-bubble  "Unable to insert the bubble. Please try again later"
                     :cant-fetch-bubbles           "Unable to fetch any bubbles at this time. Please try again later"
                     :failed-to-create-track       "Failed to create the track. Please try again later"
                     :failed-to-vote-track         "Failed to vote the track. Please try again later"
                     :enter-device                 "Enter device"}

   :pt-br           {:enter-latitude               "Informe a latitude"
                     :enter-longitude              "Informe a longitude"
                     :spotify-not-responding       "Spotify não está respondendo. Por favor, tente novamente mais tarde"
                     :cant-update-location         "Não consegui atualizar sua localização. Por favor, tente novamente mais tarde"
                     :location-updated             "Localização atualizada com sucesso"
                     :could-not-complete-action    "Não consegui realizar esta ação. Por favor, tente novamente mais tarde"
                     :incorrect-action             "uh?"
                     :invalid-login-code           "Código de login inválido. Tente autenticar novamente"
                     :invalid-state-login-code     "Código de login inválido ou expirado, Tente autenticar novamente"
                     :session-start-error          "Não foi possivel inicializar a sessão. Por favor, tente novamente mais tarde"
                     :failed-to-get-out-the-bubble "Não consegui sair da bolha. Por favor, tente novamente mais tarde"
                     :done                         "Feito !"
                     :there-is-no-music            "Não há música ainda"
                     :u-are-not-in-a-bubble        "Você não está em uma bolha"
                     :couldnt-get-playlist         "Não foi possivel buscar sua playlist. Por favor, tente novamente mais tarde"
                     :bubble-not-found             "Não foi possível encontrar a bolha. Tem certeza que ela realmente existe?"
                     :already-in-this-bubble       "Você já está nessa bolha."
                     :bubble-not-available         "Você não cumpre os requisitos para entrar nessa bolha"
                     :failed-to-get-in-the-bubble  "Falha ao entrar na bolha. Por favor, tente novamente mais tarde"
                     :failed-to-insert-the-bubble  "Não foi possivel inserir a bolha. Por favor, tente novamente mais tarde"
                     :cant-fetch-bubbles           "Não consegui buscar nenhuma bolha no momento. Por favor, tente novamente mais tarde"
                     :failed-to-create-track       "Falha ao inserir a track. Por favor, tente novamente mais tarde"
                     :failed-to-vote-track         "Falha ao votar na track. Por favor,  tente novamente mais tarde"
                     :enter-device                 "Informe o dispositivo"}
   :tongue/fallback :pt-br})

(def translate                                              ;; [locale key & args] => string
  (tongue/build-translate dicts))