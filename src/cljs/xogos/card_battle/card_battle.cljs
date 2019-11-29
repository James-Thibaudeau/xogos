(ns xogos.card-battle.card-battle
  (:require [reagent.core :as reagent]))

(def state (reagent/atom {:player-1 nil
                          :player-2 nil
                          :winner nil}))

(defn swap-winner [winner]
  (swap! state assoc :winner winner))

(defn make-card! [player-id]
  {:player-id player-id
   :card-id (str (random-uuid))
   :attack (inc (rand-int 5))
   :defense (inc (rand-int 5))})

(defn populate-board! [player-id]
  (map #(make-card! player-id) (range 5)))

(defn select-card [card]
  (swap! state assoc (:player-id card) card)
  (js/console.log @state))

(defn resolve-battle [attack defense]
  (if (>= attack defense)
    :attacker-win
    :defender-win))

(defn card [{:keys [attack defense] :as card}]
  [:div {:on-click #(select-card card)
         :style {:padding 5 :border "1px solid"}}
   [:div {:style {:color "orange"}} attack]
   [:div {:style {:color "blue"}} defense]])

(defn card-battle []
  [:div.container.is-fluid
   [:h1.title "Card Battle"]

   [:div
    [:h2.title "Player 2"]
    (map (fn [i] [card i]) (populate-board! :player-2))]

   [:div
    [:h2.title "Player 1"]
    (map (fn [i] [card i]) (populate-board! :player-1))]])