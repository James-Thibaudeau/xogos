(ns xogos.common.common
  (:require [reagent.core :as reagent]))

(defn make-grid [sx sy data-fn]
  (mapv (fn [y]
          (mapv (fn [x]
                  (data-fn x y))
                (range sx)))
        (range sy)))

(defn game-timer [game-state]
  (reagent/with-let [timer-fn (js/setInterval #(swap! game-state update :time dec) 1000)]
                    [:div.timer
                     (when (= (:time @game-state) 0)
                       (swap! game-state assoc :game-over :time-over)
                       (js/clearInterval timer-fn))]
                    (finally (js/clearInterval timer-fn))))