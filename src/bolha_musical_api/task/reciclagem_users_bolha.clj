(ns bolha-musical-api.task.reciclagem_users_bolha
  "Contem task periódica para sincronizar o player dos usuários"
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.schedule.simple :refer [repeat-forever with-interval-in-milliseconds]]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-interval-in-milliseconds]]
            [bolha-musical-api.query-defs :as query]
            [com.climate.claypoole :as cp]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [bolha-musical-api.general-functions.spotify.bolha :as gfbol]
            [clojure.tools.logging :as log]))

(defn- exec
  []
  (let [membros-invalidos (query/busca-membros-fora-range-bolha query/db {})]
    (cp/pfor 4 [membro membros-invalidos]
             (gfbol/remover-usuario-bolha (:bolha_id membro) (:user_id membro)))))

(defjob ReciclagemUserBolhas
  [ctx]
  (let [lock-key (keyword (str "lock-id-" 1))]
    (locking lock-key
      (do (log/info "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
          (log/info "<---------------- DO::ReciclagemUserBolhas       ---------------->")
          (dorun (exec))
          (log/info "<---------------- FINISHED::ReciclagemUserBolhas ---------------->")))))

(defn schecule-reciclagem
  [& m]
  (let [s (qs/start (qs/initialize))
        job (j/build
             (j/of-type ReciclagemUserBolhas)
             (j/with-identity (j/key "jobs.ReciclagemUserBolhas.1")))
        trigger (t/build
                 (t/with-identity (t/key "triggers.ReciclagemUserBolhas.1"))
                 (t/start-now)
                 (t/with-schedule (schedule
                                   (repeat-forever)
                                   (with-interval-in-milliseconds 5000))))]
    (qs/schedule s job trigger)))