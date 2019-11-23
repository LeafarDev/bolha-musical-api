(defproject bolha-musical-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [migratus "1.2.7"]
                 [com.layerware/hugsql "0.5.1"]
                 ; [org.slf4j/slf4j-log4j12 "1.7.29"]
                 [clj-spotify "0.1.9"]
                 [clj-time "0.15.2"]
                 [buddy "2.0.0"]
                 [try-let "1.3.1"]
                 [mysql/mysql-connector-java "8.0.17"]
                 [seancorfield/next.jdbc "1.0.10"]
                 [metosin/compojure-api "2.0.0-alpha30"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.1.0"]
            [lein-cljfmt "0.6.5"]
            [lein-kibit "0.1.8"]
            [lein-ring "0.12.5"]
            [migratus-lein "0.5.2"]]
  :ring {:handler bolha-musical-api.handler/app}
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}
  :uberjar-name "server.jar"
  :profiles {
             ;; Set these in ./profiles.clj
             :test-env-vars {}
             :dev-env-vars  {}
             :dev [{:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]}
                   :dev-env-vars]
             }
  )
