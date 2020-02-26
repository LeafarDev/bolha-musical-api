(ns bolha-musical-api.general-functions.date-formatters
  (:require
    [clj-time.core :as t]
    [clj-time.local :as l]
    [clj-time.format :as f]
    [clojure.tools.logging :as log])
  (:import [org.joda.time DateTimeZone LocalDateTime]))
(DateTimeZone/setDefault (DateTimeZone/forID "Etc/UCT"))
(def multi-format
  (f/formatter t/utc
               "YYYY-MM-dd"
               "yyyy-MM-dd H:mm:ss"
               "YYYY-MM-dd'T'HH:mm:ss.SSSZ"))

(defn parse [s]
  (f/parse multi-format s))

(defn parse-mysql-date-time-format
  [date]
  (clj-time.format/unparse (clj-time.format/formatter-local "yyyy-MM-dd H:mm:ss") date))

(defn local-now
  "Returns a DateTime for the current instant in the default time zone."
  []
  (parse (.toString (LocalDateTime/now (DateTimeZone/forID "Etc/UCT")) "yyyy-MM-dd H:mm:ss")))

(defn nowMysqlFormat
  []
  (parse-mysql-date-time-format (local-now)))

(defn agora-add-minutos
  [minutos]
  (t/plus (local-now) (t/minutes minutos)))

(defn meses-em-segundos
  [meses]
  (* meses 2629800))

(defn segundos-para-minutos
  [segundos]
  (/ segundos 60))

(defn date-greater?
  [date1, date2]
  (= date1 (t/latest date1 date2)))

(defn intervalo-milissegundos
  [date1 date2]
  (t/in-millis (t/interval date1 date2)))

(defn intervalo-minutos
  [date1 date2]
  (t/in-minutes (t/interval date1 date2)))

(defn intervalo-segundos
  [date1 date2]
  (t/in-seconds (t/interval date1 date2)))