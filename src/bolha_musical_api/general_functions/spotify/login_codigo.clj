(ns bolha-musical-api.general_functions.spotify.login_codigo
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

(defn state-valido-em-callback?
  "Recebo um código e verifico se é válido para ser utilizado no callback"
  [state]
  (if-let [codigo_data (not-empty (query/busca-codigo-valido query/db {:id state :agora (df/nowMysqlFormat)}))]
    (codigo_data :id)
    false))

(defn state-trocavel-por-token?
  "Pego um codigo e busco no banco onde ele tem token e não foi checkado ainda"
  [state]
  (if-let [codigo_data (not-empty (query/busca-codigo-trocavel-por-token query/db {:id state :agora (df/nowMysqlFormat)}))]
    (codigo_data :id)
    false))

(defn codigo-ja-utilizado?
  "Marca o codigo como já utilizado"
  [state]
  (= 1 (query/update-codigo-checado query/db {:id state :agora (df/nowMysqlFormat)})))