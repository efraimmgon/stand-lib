(ns stand-lib.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [stand-lib.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[stand-lib started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[stand-lib has shut down successfully]=-"))
   :middleware wrap-dev})
