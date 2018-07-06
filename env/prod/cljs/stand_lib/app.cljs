(ns stand-lib.app
  (:require [stand-lib.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
