(ns bolha-musical-api.general-functions.date-formatters
  (:require
   [clj-time.core :as t]
   [clj-time.local :as l]
   [clj-time.format :as f]))

(def multi-format
  (f/formatter t/utc
               "YYYY-MM-dd"
               "yyyy-MM-dd H:mm:ss"
               "YYYY-MM-dd'T'HH:mm:ss.SSSZ"))

(defn parse [s]
  (f/parse multi-format s))

(defn parse-mysql-date-time-format
  [date]
  (clj-time.format/unparse (clj-time.format/formatter "yyyy-MM-dd H:mm:ss") date))

(defn nowMysqlFormat
  []
  (parse-mysql-date-time-format (l/local-now)))

(defn agora-add-minutos
  [minutos]
  (t/plus (l/local-now) (t/minutes minutos)))

(defn meses-em-segundos
  [meses]
  (* meses 2629800))

(defn segundos-para-minutos
  [segundos]
  (/ segundos 60))

(defn date-greater?
  [date1, date2]
  (= date1 (t/latest date1 date2)))

(defn intervalo-minutos
  [date1 date2]
  (t/in-minutes (t/interval date1 date2)))

(defn intervalo-segundos
  [date1 date2]
  (t/in-seconds (t/interval date1 date2)))