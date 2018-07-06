(ns user
  (:require 
            [mount.core :as mount]
            [stand-lib.figwheel :refer [start-fw stop-fw cljs]]
            stand-lib.core))

(defn start []
  (mount/start-without #'stand-lib.core/repl-server))

(defn stop []
  (mount/stop-except #'stand-lib.core/repl-server))

(defn restart []
  (stop)
  (start))


