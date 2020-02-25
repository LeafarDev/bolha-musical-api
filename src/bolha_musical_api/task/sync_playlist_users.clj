(ns bolha-musical-api.task.sync-playlist-users
  "Contem task periódica para sincronizar o player dos usuários"
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.schedule.simple :refer [repeat-forever with-interval-in-milliseconds]]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-interval-in-milliseconds]]
            [bolha-musical-api.general-functions.spotify.track :refer [sincronizar-tempo-tracks
                                                                       track-terminou?
                                                                       tocar-track-para-membros
                                                                       nenhuma-tocando?
                                                                       precisa-ser-skipada?
                                                                       track-terminou?
                                                                       primeira-track-nao-tocada
                                                                       atualmente-tocando
                                                                       proxima]]
            [com.climate.claypoole :as cp]
            [clj-time.coerce :as c]
            [bolha-musical-api.query-defs :as query]
            [clojure.tools.logging :as log]
            [bolha-musical-api.task.reciclagem_users_bolha :as taskrecicla]
            [bolha-musical-api.redis-defs :refer [wcar*]]))

(defn- exec []
  (if-let [bolhas-ativas (not-empty (query/get-bolhas-ativas query/db))]
    (cp/pfor 4 [bolha bolhas-ativas]                        ;;; talvez desnecessário
             (if-let [playlist (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id (:id bolha)}))]
               (do (log/info "---------------- SINCRONIZANDO -----------------")
                   (let [sincronizadas (sincronizar-tempo-tracks playlist)]
                     (if (nenhuma-tocando? sincronizadas)
                       (if-let [track-a-tocar (not-empty (primeira-track-nao-tocada sincronizadas))]
                         (dorun (tocar-track-para-membros (:spotify_track_id track-a-tocar) (:id bolha) (:id track-a-tocar)))))
                     (if-let [atualmente-tocando (not-empty (atualmente-tocando sincronizadas))]
                       (do (log/info (str "FEVEREIRO TEM CARNAVAL"))
                           (when (or (precisa-ser-skipada? (:id atualmente-tocando) (:id bolha))
                                     (track-terminou? (c/from-sql-date (:started_at atualmente-tocando)) (:duration_ms atualmente-tocando)))
                             (query/atualiza-para-nao-execucao-track query/db (select-keys atualmente-tocando [:id]))
                             (if-let [proxima (not-empty (proxima sincronizadas (:id atualmente-tocando)))]
                               (when-not (precisa-ser-skipada? (:id proxima) (:id bolha))
                                 (log/info "running viena:: " (proxima sincronizadas (:id atualmente-tocando)))
                                 (dorun (tocar-track-para-membros (:spotify_track_id proxima) (:id bolha) (:id proxima)))
                                 true))))
                       (log/info "NADA ATUALMENTE TOCANDO:: "))))))
    (log/info "sem bolhas")))

(defjob SyncPlaylistUsersJob
  [ctx]
  (let [lock-key (keyword (str "lock-id-" 1))]
    (locking lock-key
      (do (log/info "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
          (log/info "<---------------- DO::SyncPlaylistUsersJob       ---------------->")
          (dorun (exec))
          (log/info "<---------------- FINISHED::SyncPlaylistUsersJob ---------------->")))))
(defn go
  [& m]
  (let [s (qs/start (qs/initialize))
        job (j/build
             (j/of-type SyncPlaylistUsersJob)
             (j/with-identity (j/key "jobs.SyncPlaylistUsers.1")))
        trigger (t/build
                 (t/with-identity (t/key "triggers.1"))
                 (t/start-now)
                 (t/with-schedule (schedule
                                   (repeat-forever)
                                   (with-interval-in-milliseconds 11000))))]
    (qs/schedule s job trigger)))

(defn init []
  (log/info "starting quartzite")
  (go)
  (taskrecicla/schecule-reciclagem))