(ns xogos.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [xogos.core-test]))

(doo-tests 'xogos.core-test)
