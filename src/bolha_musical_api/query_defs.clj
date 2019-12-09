(ns bolha-musical-api.query-defs
  (:require
   [hugsql.core :as hugsql]
   [environ.core :refer [env]]))

(def db (env :database-url))

(hugsql/def-db-fns "sql/spotify/login_codes.sql")
(hugsql/def-db-fns "sql/bolha/bolhas.sql")
(hugsql/def-db-fns "sql/bolha/bolhas_membros.sql")
(hugsql/def-db-fns "sql/user/user.sql")