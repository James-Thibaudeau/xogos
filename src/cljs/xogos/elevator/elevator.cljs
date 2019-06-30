(ns xogos.elevator.elevator
  (:require [reagent.core :as reagent]
            [xogos.common.common :as common]))

(def color-map {1 "#0099ff"
                2 "#00cc33"
                3 "#bb3598"
                4 "#644117"
                5 "#ff7518"})

(defn person [id]
  (let [color (get color-map id)]
    [:svg {:viewBox "0 0 200 200"
           :preserveAspectRatio "none"
           :width 50
           :height 50
           :fill color}
     [:g
      [:circle {:cx 100 :cy 50 :r 40}]
      [:text {:x "50%" :y "35%" :font-size 60 :text-anchor "middle" :fill "white"} id]]
     [:rect {:x 85 :y 85 :width 30 :height 120}]
     [:rect {:x 65 :y 100 :width 25 :height 55}]
     [:rect {:x 110 :y 100 :width 25 :height 55}]]))

(defn generate-people [n m]
  (into {}
        (map (fn [[k _]]
               [k (mapv (fn [_]
                          {:destination (inc (rand-int 5))
                           :id (random-uuid)})
                        (range n))])
             m)))

(defn new-game []
  {:floors (generate-people 5 {1 []
                               2 []
                               3 []
                               4 []
                               5 []})
   :elevator-capacity 4
   :elevator []
   :elevator-position 1
   :successes 0
   :failures 0
   :time 120
   :running? false
   :game-over false})

(def game-state (reagent/atom (new-game)))

(defn win-condition [all-floors]
  (every? (fn [[floor people]]
            (every? (fn [person]
                      (= floor (:destination person)))
                    people))
          all-floors))

(defn check-win []
  (when (and (empty? (:elevator @game-state))
             (or
               (= (:time @game-state) 0)
               (win-condition (:floors @game-state))))
    (swap! game-state assoc :game-over :win)))

(defn move-elevator! [direction]
  (let [current-position (:elevator-position @game-state)]
    (when (and
            (not= current-position 5)
            (= direction :up))
      (swap! game-state update :elevator-position inc))
    (when (and
            (not= current-position 1)
            (= direction :down))
      (swap! game-state update :elevator-position dec))))

(defn take-person-from-floor [floor]
  (when (and
          (> (count (get-in @game-state [:floors floor])) 0)
          (< (count (:elevator @game-state))
             (:elevator-capacity @game-state)))
    (reset! game-state
            (-> @game-state
                (update :elevator #(conj % (first (get-in @game-state [:floors floor]))))
                (update-in [:floors floor] #(vec (rest %)))))))

(defn drop-person-off [floor]
  (when-let [person (first (:elevator @game-state))]
    (reset! game-state
            (cond-> @game-state
                    true
                    (update :elevator #(vec (rest %)))
                    true
                    (update-in [:floors floor] #(conj % person))
                    (= floor (:destination person))
                    (update :successes inc)
                    (not= floor (:destination person))
                    (update :failures inc)))))

(defn move [e]
  (let [key (.-keyCode e)]
    (case key
      40 ;; down
      (move-elevator! :down)
      38 ;; up
      (move-elevator! :up)
      37 ;; take person
      (take-person-from-floor (:elevator-position @game-state))
      39
      (do
        (drop-person-off (:elevator-position @game-state))
        (check-win))
      nil)))

(defn game-board []
  (let [elevator-position (:elevator-position @game-state)]
    [:div.columns

     [:div.column
      (map (fn [[floor-id _]]
             (if (= floor-id elevator-position)
               ^{:key (str "efloor" floor-id)}
               (into ^{:key (str "efloor" floor-id)}
                     [:div {:style {:border "1px solid red"
                                    :height "50px"}}]
                     (map (fn [{:keys [id destination]}]
                            ^{:key (str "person " id)}
                            [person destination])
                          (reverse (:elevator @game-state))))
               ^{:key (str "efloor" floor-id)} [:div {:style {:height "50px"}}]))
           (reverse (:floors @game-state)))]

     [:div.column
      (map (fn [[floor-id people]]
             (into ^{:key (str "floor" floor-id)}
                   [:div {:style {:display "flex"
                                  :height "50px"
                                  :width "50px"}}]
                   (mapv (fn [{:keys [id destination]}]
                           ^{:key id} [:div
                                       [person destination]])
                         people)))
           (reverse (:floors @game-state)))]]))

(defn game-pad [{:keys [up down left right]}]
  [:div {:style
         {:display "flex"
          :flex-direction "column"
          :width "5em"}}
   [:button.button {:on-click up}
    [:i {:class "fas fa-arrow-up"}]]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:button.button {:on-click left}
     [:i {:class "fas fa-arrow-left"}]]
    [:button.button {:on-click right}
     [:i {:class "fas fa-arrow-right"}]]]
   [:button.button {:on-click down}
    [:i {:class "fas fa-arrow-down"}]]])

(def gamepad-fns {:up #(move-elevator! :up)
                  :down #(move-elevator! :down)
                  :left #(take-person-from-floor (:elevator-position @game-state))
                  :right #(do (drop-person-off (:elevator-position @game-state))
                              (check-win))})

(defn elevator []
  (reagent/with-let [_ (.addEventListener js/document "keydown" move)]
                    [:div.container.is-fluid
                     [:h1.title "Elevator Game"]
                     [:span.subtitle
                      (str "Successes: "
                           (:successes @game-state)
                           " Failures: "
                           (:failures @game-state)
                           " Time Remaining: "
                           (:time @game-state))]
                     (if (:game-over @game-state)
                       [:div
                        [:h2.title "GAME OVER" (when (and (= (:game-over @game-state) :win)
                                                          (= 0 (:failures @game-state))) "--PERFECT!")]
                        [:button {:on-click #(reset! game-state (new-game))} "New Game"]]
                       [:div
                        (if (:running? @game-state)
                          [:div
                           [common/game-timer game-state]
                           [game-board]
                           [game-pad gamepad-fns]]
                          [:button {:on-click #(swap! game-state assoc :running? true)} "Start Game"])])]
                    (finally (.removeEventListener js/document "keydown" move))))