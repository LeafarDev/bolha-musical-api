(ns bolha-musical-api.general-functions.rocket-chat.rocket
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.set :refer :all]
            [bolha-musical-api.util :refer [rmember]]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [clojure.tools.logging :as log]))

(defn rocket-login
  "Chama api de login do rocket chat"
  [username password]
  (-> (str (env :rocket-chat-base-url) "/api/v1/login")
      (client/post {:as          :json
                    :form-params {:user     username
                                  :password password}})
      :body
      :data))

(defn admin-token
  "Pegar o token de administrador"
  []
  (rmember "admin-token"
           3600
           (:authToken (rocket-login (env :rocket-chat-admin-username) (env :rocket-chat-admin-password)))))

(defn user-token
  "Pegar token do usuário"
  [user]
  (:authToken (rocket-login (:email user) (:rocket_chat_password user))))

(defn criar-usuario
  "Recebe objeto usuario local e cria no rocket chat"
  [data]
  (-> (str (env :rocket-chat-base-url) "/api/v1/users.create")
      (client/post {:headers     {:X-User-Id    (env :rocket-chat-admin-id)
                                  :X-Auth-Token (admin-token)}
                    :as          :json
                    :form-params data})
      :body
      :user))

(defn adicionar-usuario-canal
  "Chama api do rocket para adicionar um usuário no canal"
  [canal-id user-rocket-id]
  (-> (str (env :rocket-chat-base-url) "/api/v1/groups.invite")
      (client/post {:headers     {:X-User-Id    (env :rocket-chat-admin-id)
                                  :X-Auth-Token (admin-token)}
                    :as          :json
                    :form-params {:userId user-rocket-id
                                  :roomId canal-id}})
      :body
      :group))

(defn remover-usuario-canal
  "Chama api do rocket para remover um usuário do canal"
  [canal-id user-rocket-id]
  (-> (str (env :rocket-chat-base-url) "/api/v1/groups.kick")
      (client/post {:headers     {:X-User-Id    (env :rocket-chat-admin-id)
                                  :X-Auth-Token (admin-token)}
                    :as          :json
                    :form-params {:userId user-rocket-id
                                  :roomId canal-id}})
      :body
      :group))

(defn criar-canal
  "Chama do rocket chat api para criar o canal, utilizando referencia da bolha"
  [canal-id user]
  (let [canal (try (-> (str (env :rocket-chat-base-url) "/api/v1/groups.create")
                       (client/post {:as          :json,
                                     :headers     {:X-Auth-Token (admin-token), :X-User-Id (env :rocket-chat-admin-id)},
                                     :form-params {:name canal-id}})
                       :body
                       :group)
                   (catch Exception e (log/error e "There was an error in get-access-token-client")))]
    (when-not (= (env :rocket-chat-admin-id) (:rocket_chat_id user))
      (adicionar-usuario-canal (:_id canal) (:rocket_chat_id user)))))