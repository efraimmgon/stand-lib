(ns stand-lib.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [reagent.core :as reagent :refer [atom]]
            [stand-lib.core :as rc]))

(deftest test-home
  (is (= true true)))

