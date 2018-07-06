(ns stand-lib.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [stand-lib.core-test]))

(doo-tests 'stand-lib.core-test)

