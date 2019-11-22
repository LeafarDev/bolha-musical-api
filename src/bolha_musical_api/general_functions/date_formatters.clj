(ns bolha-musical-api.general_functions.date-formatters
  (:require
   [clj-time.core :as t]
   [clj-time.local :as l]
   [clj-time.format :as f]))

(defn parse-mysql-date-time-format
  [date]
  (clj-time.format/unparse (clj-time.format/formatter "yyyy-MM-dd H:mm:ss") date))

(defn nowMysqlFormat
  []
  (parse-mysql-date-time-format (l/local-now)))

(defn agora-add-minutos
  [minutos]
  (t/plus (l/local-now) (t/minutes minutos)))

(defn segundos-para-minutos
  [segundos]
  (/ segundos 60))