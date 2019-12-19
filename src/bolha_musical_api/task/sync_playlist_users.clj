(ns bolha-musical-api.task.sync-playlist-users
  "Contem task periódica para sincronizar o player dos usuários"
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clj-time.core :as time-clj]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [bolha-musical-api.query-defs :as query]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [clojure.tools.logging :as log]
            [clojurewerkz.quartzite.schedule.simple :refer [repeat-forever with-interval-in-milliseconds]]
            [clj-spotify.core :as sptfy]
            [clojurewerkz.quartzite.scheduler :as qs]
            [com.climate.claypoole :as cp]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [bolha-musical-api.redis_defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [bolha-musical-api.util :as u]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-interval-in-milliseconds]]))

(defn- update-time-track
  [previous-track current-track]
  (try (-> current-track
           (assoc :start-at (time-clj/plus (:end-at previous-track) (time-clj/millis 200)))
           (assoc :end-at (time-clj/plus
                           (time-clj/plus (:end-at previous-track) (time-clj/millis 200))
                           (time-clj/millis (:duration_ms previous-track)))))
       (catch Exception e
         (log/error e))))

(defn- set-time-first-track
  [first-track]
  (try (-> first-track
           (assoc :start-at (l/local-now))
           (assoc :end-at (time-clj/plus (l/local-now) (time-clj/millis (:duration_ms first-track)))))
       (catch Exception e
         (log/error e))))

(defn- track-terminou?
  [started-at duration-ms]
  ;;; quem sabe eu possa começar a chamar a pŕoxima música faltando um segundo pra diminuir a falta de sincronia ?
  (try (let [ends-at (time-clj/plus started-at (time-clj/millis duration-ms))]
         (when (df/date-greater? (l/local-now) ends-at)
           (log/warn "TERMINOU::::" (df/date-greater? (l/local-now) ends-at) (l/local-now) ends-at)
           true))
       (catch Exception e
         (log/error e))))

(defn- sincronizar-tempo-tracks
  [playlist]
  (loop [play playlist prev nil reunificados []]
    (if (nil? prev)
      (let [first-track (set-time-first-track (first play))]
        (if (empty? (rest play))
          [first-track]
          (recur (rest play) first-track (concat [] [first-track]))))
      (let [track (update-time-track prev (first play))]
        (if (empty? (rest play))
          (concat reunificados [track])
          (recur (rest play) track (concat reunificados [track])))))))

(defn- processa-track-membro
  [track-id track-id-interno device-id spotify-access-token]
  (do
    (log/info (str "spotify:track:" track-id " internal-id:" track-id-interno))
    (sptfy/start-or-resume-a-users-playback {:device_id device-id :uris [(str "spotify:track:" track-id)]} spotify-access-token)))

;;; kibit buga com wcar*
(defn- del-bolha-key
  [bolha-key]
  (wcar* (car/del bolha-key)))

(defn- tocar-track-para-membros
  [track-id bolha-id track-id-interno]
  (let [membros (query/busca-membros-bolha query/db {:bolha_id bolha-id})
        bolha-key (str "playlist-bolha-" bolha-id)]
    (query/atualiza-estado-para-execucao-track query/db {:id track-id-interno :agora (df/nowMysqlFormat)})
    (del-bolha-key bolha-key)
    (cp/pfor 4 [membro membros]
             (let [devices (sptfy/get-current-users-available-devices {} (:spotify_access_token membro))
                   primeiro-device (:id (first (:devices devices)))]
               (processa-track-membro track-id track-id-interno primeiro-device (:spotify_access_token membro))))))

(defn- nenhuma-tocando?
  [playlist]
  (empty? (filter #(= 1 (:current_playing %)) playlist)))
(defn- proxima
  [lista current-id]
  (first (filter #(> (:id %) current-id) lista)))

(defn- exec []
  (if-let [bolhas-ativas (not-empty (query/get-bolhas-ativas query/db))]
    (for [bolha bolhas-ativas]                              ;;; talvez desnecessário
      (if-let [playlist (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id (:id bolha)}))]
        (do (log/info "---------------- SINCRONIZANDO -----------------")
            (let [sincronizadas (sincronizar-tempo-tracks playlist)]
              (u/go-for-loop [track-sincronizada sincronizadas] ;;; remover esse loop, é desnecessauro
                             (if (nenhuma-tocando? sincronizadas) ;;; isso não precisa executar dentro do loop
                               (do (log/info "nenhuma-tocando?:: true") ;;; levar em conta as que já tocaram
                                   (dorun (tocar-track-para-membros (:spotify_track_id track-sincronizada) (:id bolha) (:id track-sincronizada)))
                                   (break))
                               ;; isso não precisa do loop, um search já resolve
                               (if (= 1 (:current_playing track-sincronizada)) ;;; REVER
                                 (when (track-terminou? (c/from-sql-date (:started_at track-sincronizada)) (:duration_ms track-sincronizada))
                                   (if-let [proxima (not-empty (proxima sincronizadas (:id track-sincronizada)))]
                                     (do (log/info "running viena:: " (proxima sincronizadas (:id track-sincronizada)))
                                         (query/atualiza-para-nao-execucao-track query/db (select-keys track-sincronizada [:id]))
                                         (dorun (tocar-track-para-membros (:spotify_track_id proxima) (:id bolha) (:id proxima)))
                                         (break)
                                         true))))))))))
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
  (go))