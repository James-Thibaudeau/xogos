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
  {:player-1 {:board []
              :attacker nil
              :defender nil
              :score 0}
   :player-2 {:board []
              :attacker nil
              :defender nil
              :score 0}
   :game {:turn nil
          :phase :idle}
   :winner nil})

(def state (reagent/atom initial-state))

(def phases {:idle :select-attacker
             :select-attacker :select-defender
             :select-defender :idle})

(defn make-card! [player-id]
  {:id (str (random-uuid))
   :player-id player-id
   :attack (inc (rand-int 5))
   :defense (inc (rand-int 5))})

(defn populate-board! [player-id]
  (mapv #(make-card! player-id) (range 5)))

(defn decide-first-player []
  (rand-nth [:player-1 :player-2]))

(defn start-game []
  (reset! state (-> initial-state
                    (assoc-in [:game :turn] (decide-first-player))
                    (assoc-in [:game :phase] :select-attacker)
                    (assoc-in [:player-1 :board] (populate-board! :player-1))
                    (assoc-in [:player-2 :board] (populate-board! :player-2)))))

(defn set-turn [player]
  (swap! state update-in [:game] assoc :turn player))

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

(defn change-turn! []
  (if (= (-> @state :game :turn) :player-1)
    (set-turn :player-2)
    (set-turn :player-1)))

(defn process-actions []
  (let [{p1-a :attacker p1-d :defender} (:player-1 @state)
        {p2-a :attacker p2-d :defender} (:player-2 @state)]
    ;; TODO: finish this fn
    ))

(defn select-card [card]
  (let [{:keys [turn phase]} (:game @state)
        player (-> card :player-id)]
    (when (= player turn)
      (case phase
        :select-attacker
        (do
          (update-attacker player card)
          (next-phase phase))
        :select-defender
        (do
          (update-defender player card)
          (next-phase phase))
        :idle
        (process-actions)))))

(defn resolve-battle [attack defense]
  (if (>= attack defense)
    :attacker-win
    :defender-win))

(defn card [{:keys [attack defense player-id] :as card} turn]
  (js/console.log card turn)
  [:div {:on-click #(select-card card)
         :style {:display "inline-block" :padding 5 :border "1px solid"}}
   (if (= turn player-id)
     [:<>
      [:div {:style {:color "orange"}} attack]
      [:div {:style {:color "blue"}} defense]]
     [:<>
      [:div "X"]
      [:div "X"]])])

(defn card-battle []
  [:div.container.is-fluid
   [:h1.title "Card Battle"]
   [:div (str @state)]
   [:button {:on-click #(start-game)} "Start Game"]

   [:div
    [:h2.title "Player 2"]
    (map (fn [c] ^{:key (:id c)} [card c (-> @state :game :turn)]) (-> @state :player-2 :board))
    [:div
     [:div
      [:p "Attacker"]
      [:div (or (-> @state
                    :player-2
                    :attacker
                    str)
                nil)]]
     [:div
      [:p "Defender"]
      [:div (or (-> @state
                    :player-2
                    :defender
                    str)
                nil)]]]]

   [:div
    [:h2.title "Player 1"]
    (map (fn [c] ^{:key (:id c)} [card c (-> @state :game :turn)]) (-> @state :player-1 :board))
    [:div
     [:div
      [:p "Attacker"]
      [:div (or (->
                  @state
                  :player-1
                  :attacker
                  str)
                nil)]]
     [:div
      [:p "Defender"]
      [:div (or (-> @state
                    :player-1
                    :defender
                    str)
                nil)]]]]])