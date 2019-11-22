(ns bolha-musical-api.route_functions.spotify.login_codigo
  (:require [bolha-musical-api.general_functions.date-formatters :as df]
            [bolha-musical-api.query_defs :as query]))

(defn criar-novo-codigo-de-login
  "Cria um uuid no banco e o retorna"
  []
  (let [novo-codigo-login (str (java.util.UUID/randomUUID))]
    (query/criar-novo-codigo-de-login
     query/db
     {:id         novo-codigo-login
      :expires_at (df/parse-mysql-date-time-format (df/agora-add-minutos 15))
      :created_at (df/nowMysqlFormat)})
    novo-codigo-login))

(defn vericar-codigo-state-eh-valido
  "Recebo um código e verifico se é válido para ser utilizado no callback"
  [state]
  (if-let [codigo_data (not-empty (query/busca-codigo-valido query/db {:id state :agora (df/nowMysqlFormat)}))]
    (codigo_data :id)
    false))

;(defn checkin-codigo-de-login
;  "Recebo código de login do spotify e processo as informações no usuário"
;  ())

