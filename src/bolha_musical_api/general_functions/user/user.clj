(ns bolha-musical-api.general-functions.user.user
  (:require
   [ring.util.http-response :refer :all]
   [bolha-musical-api.query-defs :as query]
   [bolha-musical-api.general-functions.date-formatters :as df]
   [bolha-musical-api.general-functions.user.create-token :as ct]
   [bolha-musical-api.util :as util]
   [clojure.set :refer :all]))

(defn get-user-by-email
  "Pego Usuário pelo seu email se existir ou retono nil"
  [email]
  (when-let [user (not-empty (query/get-user-by-email query/db {:email email}))]
    user))

(defn get-user-by-state
  "Busco um usuário pelo seu state"
  [state]
  (when-let [user (not-empty (query/get-user-by-state query/db {:state state}))]
    user))

(defn expire-at-handle
  "Conversão do expire_in recebido do spotify"
  [expires-in]
  (df/parse-mysql-date-time-format (df/agora-add-minutos (df/segundos-para-minutos expires-in))))

(defn converte-user-me-spotify-em-dado-local
  "Pego '/me' do spotify e transformo em mapa compativel com dados do banco"
  [user-me]
  (-> user-me
      (assoc :language_code (str (util/get-country-language (:country user-me))))
      (rename-keys {:id :spotify_client_id :country :country_code})
      (select-keys [:spotify_client_id :email :country_code :language_code])))

(defn converte-token-data-spotify-em-dado-local
  "Pego '/token' do spotify e transformo em uma estrutura compativel com dados do banco"
  [token-data]
  (-> token-data
      (assoc :spotify_token_expires_at (expire-at-handle (token-data :expires_in)))
      (rename-keys {:access_token :spotify_access_token :scope :spotify_scope :refresh_token :spotify_refresh_token})
      (select-keys [:spotify_access_token :spotify_scope :spotify_refresh_token :spotify_token_expires_at])))

(defn junta-dados-spotify
  "Pego dados do spotify e transformo em uma estrutura compativel com colunas do banco"
  ([user-me token-data]
   (conj
    (converte-user-me-spotify-em-dado-local user-me)
    (converte-token-data-spotify-em-dado-local token-data)))
  ([user-me token-data user-local]
   (conj
    (select-keys user-local [:id])
    (converte-user-me-spotify-em-dado-local user-me)
    (converte-token-data-spotify-em-dado-local token-data))))

(defn transforma-spotify-me-em-rocket-data
  "Pego o 'me' do spotify e transformo em estrutura compativel para criar usuário no chat"
  [user-me]
  (-> user-me
      (rename-keys {:display_name :name
                    :id           :username})
      (assoc :password (str (java.util.UUID/randomUUID)))
      (select-keys [:email :name :password :username])))

(defn cria-usuario-banco-callback
  "Recebo os dados do usuário e o insiro na base, no fim retorno o token"
  [dados]
  (if-let [insert-result (= 1 (query/insert-user-spotify-callback query/db dados))]
    (let [usuario-criado (get-user-by-email (:email dados))]
      (ct/criar-token-user usuario-criado))
    false))

(defn atualiza-usuario-banco-callback
  "Recebo os dados do usuário e o insiro na base, no fim retorno o token"
  [dados]
  (if-let [update-result (= 1 (query/update-user-spotify-callback query/db dados))]
    (let [usuario-atualizado (get-user-by-email (:email dados))]
      (ct/criar-token-user usuario-atualizado))
    false))

