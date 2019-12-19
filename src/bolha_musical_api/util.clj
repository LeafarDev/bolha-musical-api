(ns bolha-musical-api.util
  "Common utility functions useful throughout the codebase."
  (:require [clojure
             [string :as str]]
            [flatland.ordered.map :refer [ordered-map]]
            [ring.util.http-response :refer :all]
            [bolha-musical-api.redis_defs :refer [wcar*]]
            [taoensso.carmine :as car :refer (wcar)]
            [bolha-musical-api.general-functions.spotify.track :refer [relacionar-tracks-local-com-spotify]]
            [clojure.tools.logging :as log])
  (:import org.apache.commons.validator.routines.UrlValidator))

(defn ^:deprecated rpartial
  "Like `partial`, but applies additional args *before* BOUND-ARGS.
   Inspired by [`-rpartial` from dash.el](https://github.com/magnars/dash.el#-rpartial-fn-rest-args)

    ((partial - 5) 8)  -> (- 5 8) -> -3
    ((rpartial - 5) 8) -> (- 8 5) -> 3

  DEPRECATED: just use `#()` function literals instead. No need to be needlessly confusing."
  [f & bound-args]
  (fn [& args]
    (apply f (concat args bound-args))))

(defn get-country-language
  "Verifica se é BR e retorna pt-br, por enquanto, para outros países apenas en"
  [country-code]
  (if (= country-code "BR")
    :pt-br
    :en))

(defn email?
  "Is `s` a valid email address string?"
  ^Boolean [^String s]
  (boolean (when (string? s)
             (re-matches #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
                         (str/lower-case s)))))

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  {:style/indent 0}
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(defn format-bytes
  "Nicely format `num-bytes` as kilobytes/megabytes/etc.

    (format-bytes 1024) ; -> 2.0 KB"
  [num-bytes]
  (loop [n num-bytes [suffix & more] ["B" "KB" "MB" "GB"]]
    (if (and (seq more)
             (>= n 1024))
      (recur (/ n 1024.0) more)
      (format "%.1f %s" n suffix))))

(defmacro varargs
  "Make a properly-tagged Java interop varargs argument. This is basically the same as `into-array` but properly tags
  the result.

    (u/varargs String)
    (u/varargs String [\"A\" \"B\"])"
  {:style/indent 1}
  [klass & [objects]]
  (vary-meta `(into-array ~klass ~objects)
             assoc :tag (format "[L%s;" (.getCanonicalName ^Class (ns-resolve *ns* klass)))))

(defn url?
  "Is `s` a valid HTTP/HTTPS URL string?"
  ^Boolean [s]
  (let [validator (UrlValidator. (varargs String ["http" "https"]) UrlValidator/ALLOW_LOCAL_URLS)]
    (.isValid validator (str s))))

(defn maybe?
  "Returns `true` if X is `nil`, otherwise calls (F X).
   This can be used to see something is either `nil` or statisfies a predicate function:

     (string? nil)          -> false
     (string? \"A\")        -> true
     (maybe? string? nil)   -> true
     (maybe? string? \"A\") -> true

   It can also be used to make sure a given function won't throw a `NullPointerException`:

     (str/lower-case nil)            -> NullPointerException
     (str/lower-case \"ABC\")        -> \"abc\"
     (maybe? str/lower-case nil)     -> true
     (maybe? str/lower-case \"ABC\") -> \"abc\"

   The latter use-case can be useful for things like sorting where some values in a collection
   might be `nil`:

     (sort-by (partial maybe? s/lower-case) some-collection)"
  [f x]
  (or (nil? x)
      (f x)))

(defmacro pdoseq
  "(Almost) just like `doseq` but runs in parallel. Doesn't support advanced binding forms like `:let` or `:when` and
  only supports a single binding </3"
  {:style/indent 1}
  [[binding collection] & body]
  `(dorun (pmap (fn [~binding]
                  ~@body)
                ~collection)))

(defmacro go-for-loop [loop-definition & body]
  `(let [continue# (atom true)
         ~'break (fn [] (swap! continue# (constantly false)))]
     (doseq [~@loop-definition :while @continue#]
       ~@body)))
(defn run-when-function
  "Run when f is function, return when it's not"
  [f]
  (if (fn? f)
    (f)
    f))
(defn rmember
  [key seconds f]
  (if-let [data (not-empty (wcar* (car/get key)))]
    data
    (last (wcar* (car/set key (run-when-function f))
                 (car/expire key seconds)
                 (car/get key)))))