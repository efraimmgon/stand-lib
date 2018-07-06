(ns stand-lib.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[stand-lib started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[stand-lib has shut down successfully]=-"))
   :middleware identity})
