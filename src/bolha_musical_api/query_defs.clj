(ns bolha-musical-api.query_defs
  (:require
   [hugsql.core :as hugsql]
   [environ.core :refer [env]]))

(def db (env :database-url))

(hugsql/def-db-fns "sql/spotify/login_codes.sql")
(hugsql/def-db-fns "sql/user/user.sql")