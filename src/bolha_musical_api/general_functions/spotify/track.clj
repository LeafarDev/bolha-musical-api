(ns bolha-musical-api.general-functions.spotify.track
  (:require [clj-http.client :as client]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.util :refer [partition-by-max-sized-piece]]
            [bolha-musical-api.query-defs :as query]
            [clj-time.core :as time-clj]
            [clj-time.local :as l]
            [clj-spotify.core :as sptfy]
            [bolha-musical-api.general-functions.date-formatters :as df]
            [clj-time.coerce :as c]
            [com.climate.claypoole :as cp]
            [bolha-musical-api.redis-defs :refer [wcar*]]
            [bolha-musical-api.util :refer [rmember]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.set :refer :all]
            [clojure.tools.logging :as log]))

(defn nenhuma-tocando?
  [playlist]
  (empty? (filter #(= 1 (:current_playing %)) playlist)))

(defn proxima
  [playlist current-id]
  (first (filter #(> (:id %) current-id) playlist)))

(defn atualmente-tocando
  [playlist]
  (first (filter #(= 1 (:current_playing %)) playlist)))

(defn precisa-ser-skipada?
  [track-id bolha-id]
  (let [qtd-membros-ativos (:qtd (query/qtd-membros-ativos-bolha query/db {:bolha_id bolha-id}))]
    (when (pos? qtd-membros-ativos)
      (let [votos-validos (query/get-votos-track-playlist-validos query/db {:track_interno_id track-id})
            qtd-votos-validos (count votos-validos)
            qtd-cimavotos (count (filter #(true? (:cimavoto %)) votos-validos))
            qtd-baixavotos (- qtd-votos-validos qtd-cimavotos)
            porcentagem-membros-votaram (* (/ qtd-votos-validos qtd-membros-ativos) 100.0)]
        (when (and (>= porcentagem-membros-votaram 40.0) (> qtd-baixavotos qtd-cimavotos))
          true)))))

(defn primeira-track-nao-tocada
  [playlist]
  (first (filter #(and (nil? (:started_at %))
                       (not (precisa-ser-skipada? (:id %) (:bolha_id %))))
                 playlist)))

(defn device-id-or-first-existent-id
  "Retorno o device-id solicitado pelo o usuário"
  [membro]
  (let [current-device-id (:spotify_current_device membro)
        devices (:devices (sptfy/get-current-users-available-devices {} (:spotify_access_token membro)))]
    (if (not-empty devices)
      (if (not-empty (filter #(= (:id %) current-device-id) devices))
        current-device-id
        (:id (first devices)))
      (do (query/update-user-spotify-current-device query/db {:spotify_current_device nil :id (:user_id membro)})
          nil))))

;;; kibit buga com wcar*
(defn- del-key
  [key]
  (wcar* (car/del key)))

(defn get-user-top-tracks
  "top tracks do usuário"
  [token]
  (-> "https://api.spotify.com/v1/me/top/tracks"
      (client/get {:headers {:Authorization (str "Bearer " token)}
                   :as      :json})
      :body))

(defn cachear-tracks
  [tracks]
  (doseq [track tracks]
    (do (print (str "| cacheando " (:id track)))
        (wcar* (car/set (:id track) track)))))

(defn get-several-tracks
  [tracks-ids token]
  (let [ids-sem-cache (filter (fn* [ids] (zero? (wcar* (car/exists ids)))) tracks-ids)
        ids-com-cache (filter (fn* [ids] (= 1 (wcar* (car/exists ids)))) tracks-ids)
        tracks-do-cache (map (fn* [ids] (wcar* (car/get ids))) ids-com-cache)
        tracks-do-spotify (sptfy/get-several-tracks {:ids (clojure.string/join "," ids-sem-cache)} token)
        todas-tracks (concat tracks-do-cache (:tracks tracks-do-spotify))]
    (cachear-tracks (:tracks tracks-do-spotify))
    (remove nil? todas-tracks)))

(defn relacionar-tracks-local-com-spotify
  [bolha-id spotify-access-token]
  (if-let [tracks-bancos (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id bolha-id}))]
    (let [ids (map :spotify_track_id (doall tracks-bancos))
          tracks-spotify (get-several-tracks ids spotify-access-token)
          tracks-bancos-resumidas (map #(-> %
                                            (rename-keys {:id :id_interno})
                                            (select-keys [:id_interno
                                                          :started_at
                                                          :current_playing
                                                          :bolha_id]))
                                       (doall tracks-bancos))]
      (map conj tracks-spotify tracks-bancos-resumidas))))

(defn check-users-saved-tracks
  [tracks-ids spotify-access-token bolha-id]
  (let [pedacos-tracks (partition-by-max-sized-piece 50 tracks-ids)
        relacao-curtidas (rmember (str "saved-" spotify-access-token "-" bolha-id) 60
                                  #(map (fn* [pedaco] (sptfy/check-users-saved-tracks {:ids (clojure.string/join "," pedaco)} spotify-access-token)) pedacos-tracks))]
    (map #(zipmap [:saved] [%]) (reduce concat relacao-curtidas))))

(defn relacionar-tracks-playlist-user-saved
  [tracks-playlist token bolha-id]
  (let [ids (map :id (doall tracks-playlist))]
    (check-users-saved-tracks ids token bolha-id)))

(defn votos-tracks-playlist
  [tracks-playlist]
  (map #(zipmap [:votos] [(rmember (str "track-bolha-votos-" (:id_interno %))
                                   10
                                   (query/get-votos-track query/db {:track_interno_id (:id_interno %)}))])
       tracks-playlist))

(defn track-terminou?
  [started-at duration-ms]
  ;;; quem sabe eu possa começar a chamar a pŕoxima música faltando um segundo pra diminuir a falta de sincronia ?
  (try (let [ends-at (time-clj/plus started-at (time-clj/millis duration-ms))]
         (if (df/date-greater? (l/local-now) ends-at)
           (do (log/warn "TERMINOU::::"
                         (df/date-greater? (l/local-now) ends-at)
                         (l/local-now) ends-at)
               true)
           (log/info (str "NÃO TERMINOU::::" started-at duration-ms))))
       (catch Exception e
         (log/error e))))

(defn set-time-first-track
  [first-track]
  (try (-> first-track
           (assoc :start-at (l/local-now))
           (assoc :end-at (time-clj/plus (l/local-now) (time-clj/millis (:duration_ms first-track)))))
       (catch Exception e
         (log/error e))))

(defn update-time-track
  [previous-track current-track]
  (try
    (let [track-sincronizada (as-> current-track track
                               (assoc track :start-at (time-clj/plus (:end-at previous-track) (time-clj/millis 200)))

                               (assoc track :end-at (time-clj/plus
                                                     (time-clj/plus (:end-at previous-track) (time-clj/millis 200))
                                                     (time-clj/millis (:duration_ms previous-track)))))]
      (if (= 1 (:current_playing current-track))
        (assoc track-sincronizada :current-position-ms (df/intervalo-milissegundos (c/from-sql-date (:started_at track-sincronizada))
                                                                                   (l/local-now)))
        track-sincronizada))
    (catch Exception e
      (log/error ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
      (log/error (str "ERRR::update-time-track::" e previous-track current-track))
      (log/error ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::"))))

(defn sincronizar-tempo-tracks
  [playlist]
  (loop [play playlist prev nil reunificados []]
    (if (nil? prev)
      (let [first-track (set-time-first-track (first play))]
        (recur (rest play) first-track (concat [] [first-track])))
      (let [track (update-time-track prev (first play))]
        (if (empty? (rest play))
          (concat reunificados [track])
          (recur (rest play) track (concat reunificados [track])))))))

(defn membro-esta-sumido?
  [data_ultima_localizacao]
  (>= 30 (df/intervalo-segundos (c/from-sql-date data_ultima_localizacao) (l/local-now))))

(defn start-or-resume-a-users-playback-with-position-ms
  "top tracks do usuário"
  [device_id uri position_ms user-token]
  (try (client/put "https://api.spotify.com/v1/me/player/play"
                   {:as           :json,
                    :headers      {:Authorization (str "Bearer " user-token)},
                    :form-params  {:position_ms position_ms,
                                   :uris        [uri]},
                    :query-params {:device_id device_id},
                    :content-type :json})
       (catch Exception e (log/error e "There was an error in start-or-resume-a-users-playback-with-position-ms"))))

(defn- processa-track-membro
  ([track-id track-id-interno device-id spotify-access-token current-position-ms]
   (do
     (log/info (str "spotify:track:" track-id " internal-id:" track-id-interno "current-position-ms:" current-position-ms))
     (start-or-resume-a-users-playback-with-position-ms device-id
                                                        (str "spotify:track:" track-id)
                                                        current-position-ms
                                                        spotify-access-token)))

  ([track-id track-id-interno device-id spotify-access-token]
   (do
     (log/info (str "spotify:track:" track-id " internal-id:" track-id-interno))
     (sptfy/start-or-resume-a-users-playback {:device_id device-id :uris [(str "spotify:track:" track-id)]} spotify-access-token))))

(defn resumir-track-user
  "Resumir track com o usuário"
  [user-id]
  (let [bolha (query/get-bolha-atual-usuario query/db {:user_id user-id})
        user (query/get-user-by-id query/db {:id user-id})
        membro-user (query/busca-membro-by-user-id query/db {:bolha_id (:id bolha) :user_id (:id user)})]
    (if-let [playlist (not-empty (query/get-tracks-by-bolha-id query/db {:bolha_id (:id bolha)}))]
      (let [sincronizadas (sincronizar-tempo-tracks playlist)]
        (if-let [track-atualmente-tocando (not-empty (atualmente-tocando sincronizadas))]
          (let [device-id (device-id-or-first-existent-id membro-user)]
            (when-not (false? (:tocar_track_automaticamente membro-user))
              (log/info (str "VAMOS VOLTAR A PILANTRAGEM::"
                             (:spotify_track_id track-atualmente-tocando) " | "
                             (:id track-atualmente-tocando) " | "
                             device-id " | "
                             (:spotify_access_token user) " |_ "
                             (:current-position-ms track-atualmente-tocando)))
              (processa-track-membro (:spotify_track_id track-atualmente-tocando)
                                     (:id track-atualmente-tocando)
                                     device-id
                                     (:spotify_access_token user)
                                     (:current-position-ms track-atualmente-tocando)))))))))

(defn tocar-track-para-membros
  [track-id bolha-id track-id-interno]
  (let [membros (query/busca-membros-bolha query/db {:bolha_id bolha-id})
        bolha-key (str "playlist-bolha-" bolha-id)
        votos-bolha-key (str "playlist-bolha-votos-" bolha-id)]
    (query/atualiza-estado-para-execucao-track query/db {:id track-id-interno :agora (df/nowMysqlFormat)})
    (del-key bolha-key)
    (del-key votos-bolha-key)
    (cp/pfor 4 [membro membros]
             (log/warn (str (membro-esta-sumido? (:data_ultima_localizacao membro))
                            (:tocar_track_automaticamente membro)))
             (when-not (and (membro-esta-sumido? (:data_ultima_localizacao membro))
                            (false? (:tocar_track_automaticamente membro)))
               (let [device-id (device-id-or-first-existent-id membro)]
                 (when-not (nil? device-id)
                   (log/info (str track-id " " track-id-interno " " device-id " " (:spotify_access_token membro)))
                   (processa-track-membro track-id track-id-interno device-id (:spotify_access_token membro))))))))

