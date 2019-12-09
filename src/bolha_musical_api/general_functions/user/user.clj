(ns bolha-musical-api.general-functions.user.user
  (:require
   [ring.util.http-response :refer :all]
   [bolha-musical-api.query-defs :as query]
   [environ.core :refer [env]]
   [bolha-musical-api.general-functions.date-formatters :as df]
   [bolha-musical-api.general-functions.user.create-token :as ct]
   [clojure.set :refer :all]))

(defn get-user-by-email
  "Pego Usuário pelo seu email se existir ou retono nil"
  [email]
  (when-let [user (not-empty (query/get-user-by-email query/db {:email email}))] user))
(defn get-user-by-state
  "Busco um usuário pelo seu state"
  [state]
  (when-let [user (not-empty (query/get-user-by-state query/db {:state state}))] user))
(defn expire-at-handle
  "Conversão do expire_in recebido do spotify"
  [expires_in]
  (df/parse-mysql-date-time-format (df/agora-add-minutos (df/segundos-para-minutos expires_in))))
(expire-at-handle 3600)

(defn converte-user-me-spotify-em-dado-local
  "Pego '/me' do spotify e transformo em mapa compativel com dados do banco"
  [user_me]
  (-> user_me
      (rename-keys {:id :spotify_client_id})
      (select-keys [:spotify_client_id :email])))

(defn converte-token-data-spotify-em-dado-local
  "Pego '/token' do spotify e transformo em mapa compativel com dados do banco"
  [token_data]
  (-> token_data
      (assoc  :spotify_token_expires_at (expire-at-handle (token_data :expires_in)))
      (rename-keys {:access_token :spotify_access_token :scope :spotify_scope :refresh_token :spotify_refresh_token})
      (select-keys [:spotify_access_token :spotify_scope :spotify_refresh_token :spotify_token_expires_at])))

(defn junta-dados-spotify
  "Pego dados do spotify e transformo em mapas compativeis com colunas do banco"
  ([user_me token_data]
   (conj
    (converte-user-me-spotify-em-dado-local user_me)
    (converte-token-data-spotify-em-dado-local token_data)))
  ([user_me token_data user_local]
   (conj
    (select-keys user_local [:id])
    (converte-user-me-spotify-em-dado-local user_me)
    (converte-token-data-spotify-em-dado-local token_data))))

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

