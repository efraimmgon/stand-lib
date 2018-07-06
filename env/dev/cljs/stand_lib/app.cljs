(ns ^:figwheel-no-load stand-lib.app
  (:require [stand-lib.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
