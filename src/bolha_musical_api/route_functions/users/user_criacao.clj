(ns bolha-musical-api.route_functions.users.user_criacao
  (:require [clj-spotify.core :as sptfy]
            [bolha-musical-api.route_functions.spotify.access_token :as sat]
            [bolha-musical-api.route_functions.spotify.login_codigo :as loginCode]
            [bolha-musical-api.query_defs :as query]
            [environ.core :refer [env]]
            [bolha-musical-api.general_functions.date-formatters :as df]
            [clojure.set :refer :all]))

; verifico se codigo é valido ok
; pego o token do usuário
; chamo o "me" do usuário
; verifico se usuário já existe
; , se não existe, eu crio e alimento os dados do token, email e expiration e gero um password
; retorno o token do usuário, junto com o "me"
(defn get-user-by-email
  "Pego Usuário pelo seu email se existir ou retono nil"
  [email]
  (if-let [user (not-empty (query/get-user-by-email query/db {:email email}))]
    user
    nil))

(defn update-user-spotify-apartir-do-callback
  "docstring"
  [user_data]
  nil)

(defn criar-usuario-spotify-apartir-do-callback
  "docstring"
  [arglist])

(defn expire-at-handle
  "docstring"
  [expires_in]
  (df/parse-mysql-date-time-format (df/agora-add-minutos (df/segundos-para-minutos expires_in))))
(expire-at-handle 3600)

(defn converte-user-me-spotify-em-dado-local
  "Pego '/me' do spotify e transformo em mapa compativel com dados do banco"
  [user_me]
  (-> user_me
      (rename-keys {:id :spotify_client_id})
      (select-keys [:spotify_client_id :email])))

(defn converte-token_data-spotify-em-dado-local
  "Pego '/token' do spotify e transformo em mapa compativel com dados do banco"
  [token_data]
  (-> token_data
      (rename-keys {:access_token :spotify_access_token :scope :spotify_scope :refresh_token :spotify_refresh_token})
      (select-keys [:spotify_access_token :spotify_scope :spotify_refresh_token])))

(defn junta-dados-spotify
  "Pego dados do spotify e transformo em mapas compativeis com colunas do banco"
  ([user_me token_data]
   (conj
    (converte-user-me-spotify-em-dado-local user_me)
    (converte-token_data-spotify-em-dado-local token_data)
    {:spotify_token_expires_at (expire-at-handle (token_data :expires_in))}))
  ([user_me token_data user_local]
   (conj
    (select-keys user_local [:id])
    (converte-user-me-spotify-em-dado-local user_me)
    (converte-token_data-spotify-em-dado-local token_data)
    {:spotify_token_expires_at (expire-at-handle (token_data :expires_in))})))

(defn criar-usuario-spotify-callback
  "Recebo parametros do callback do spotify e crio o usuário"
  [code, state]
  (if (not= false (loginCode/vericar-codigo-state-eh-valido state))
    (let [token_data (sat/get-access-token-client code)
          user_me (sptfy/get-current-users-profile {} (str (token_data :access_token)))]
      (if-let [user_local (not-empty (get-user-by-email (user_me :email)))]
        (junta-dados-spotify user_me token_data user_local)
        (junta-dados-spotify user_me token_data)))
    false))