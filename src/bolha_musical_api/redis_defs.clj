(ns bolha-musical-api.redis_defs
  (:require
   [taoensso.carmine :as car :refer (wcar)]
   [environ.core :refer [env]]))

(def server-conn {:pool {} :spec {:uri  (env :redis-url)}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))
