(ns xogos.card-battle.card-battle
  (:require [reagent.core :as reagent]))

; steps for this game
; start game
; decide first player
; set turn to first player
; populate boards
; first player selects their attacker
; first player selects their defender
; second player selects their attacker
; second player selects their defender
; resolve battle
; remove the losing cards
;
; phases - select-attacker, select-defender, idle

(def initial-state
  {:player-1 {:board nil
              :attacker nil
              :defender nil
              :score 0}
   :player-2 {:board nil
              :attacker nil
              :defender nil
              :score 0}
   :game {:turn nil
          :phase :idle}
   :winner nil})

(def phases {:idle :select-attacker
             :select-attacker :select-defender
             :select-defender :idle})

(def state (reagent/atom initial-state))

(defn next-phase [current-phase]
  (swap! state update-in [:game] assoc :phase (current-phase phases)))

(defn set-winner [winner]
  (swap! state assoc :winner winner))

(defn increment-score [player]
  (swap! state update-in [player :score] inc))

(defn update-attacker [player card]
  (swap! state update-in [player] assoc :attacker card))

(defn update-defender [player card]
  (swap! state update-in [player] assoc :defender card))

(defn decide-winner [p1-score p2-score]
  (let [result (cond
                  (> p1-score p2-score) :player-1
                  (> p2-score p1-score) :player-2
                  :else :tie)]
    (swap! state assoc :winner result)))

(defn select-card [card]
  (swap! state assoc (:player-id card) card))

(defn make-card! [player-id]
  {:player-id player-id
   :card-id (str (random-uuid))
   :attack (inc (rand-int 5))
   :defense (inc (rand-int 5))})

(defn populate-board! [player-id]
  (map #(make-card! player-id) (range 5)))

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