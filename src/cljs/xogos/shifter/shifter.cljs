(ns xogos.shifter.shifter
  (:require [reagent.core :as reagent]
            [xogos.common.common :as common]))


(def color-map {1 "#0099ff"
                2 "#00cc33"
                3 "#bb3598"
                4 "#644117"
                5 "#ff7518"})

(defn cell-data [_ _]
  {:id (random-uuid)
   :value (inc (rand-int 5))})

(defn make-row []
  (mapv cell-data (range 5)))

(defn make-grid [sx sy data-fn]
  (mapv (fn [y]
          (mapv (fn [x]
                  (data-fn x y))
                (range sx)))
        (range sy)))

(defn get-column [index grid]
  (reduce (fn [prev item]
            (conj prev (nth item index)))
          []
          grid))

(defn columns [grid]
  (map (fn [i]
         (get-column i grid))
       (range (count grid))))

(defn get-row [idx grid]
  (nth grid idx))

(defn rotate-left [n coll]
  (let [c (mod n (count coll))]
    (into []
          (concat (drop c coll) (take c coll)))))

(defn rotate-right [n coll]
  (rotate-left (- n) coll))

(defn completed-value [coll]
  (->> (range 1 6)
       (map (fn [i] (when (apply = i coll) i)))
       (remove nil?)
       first))

(defn get-completions [coll]
  (map-indexed
    (fn [idx item]
      (let [complete (completed-value (map :value item))]
        (when complete
          [idx complete])))
    coll))

(defn complete-columns [grid]
  (->> grid
       columns
       get-completions
       (remove nil?)))

(defn complete-rows [grid]
  (->> grid
       get-completions
       (remove nil?)))

(defn new-game []
  {:grid (make-grid 5 5 cell-data)
   :row 2
   :col 2
   :win-total 4
   :cursor-type :move
   :completions {1 0
                 2 0
                 3 0
                 4 0
                 5 0}
   :time 120
   :game-over false
   :running? false})

(def game-state (reagent/atom (new-game)))

(defn update-row [row-idx row]
  (swap! game-state update :grid #(assoc % row-idx row)))

(defn update-column [col-idx col grid]
  (swap! game-state assoc :grid
         (into []
               (map-indexed
                 (fn [idx row]
                   (assoc row col-idx (nth col idx)))
                 grid))))

(defn check-for-completions [grid]
  (let [complete-cols (into {} (complete-columns grid))
        complete-rows (into {} (complete-rows grid))
        complete-vals (concat (vals complete-rows) (vals complete-cols))
        updated-completions (reduce (fn [prev i]
                                      (update prev i inc))
                                    (:completions @game-state)
                                    complete-vals)]
    (swap! game-state assoc :completions updated-completions)

    (when (seq complete-cols)
      (doseq [[idx _] complete-cols]
        (update-column idx (make-row) (:grid @game-state))))
    (when (seq complete-rows)
      (doseq [[idx _] complete-rows]
        (update-row idx (make-row))))))

(defn check-win []
  (when
    (every? (fn [[_ v]]
              (> v (:win-total @game-state)))
            (:completions @game-state))
    (swap! game-state assoc :game-over :win)))


(defn move-up! []
  (swap! game-state update :row #(mod (dec %) 5)))

(defn move-down! []
  (swap! game-state update :row #(mod (inc %) 5)))

(defn move-left! []
  (swap! game-state update :col #(mod (dec %) 5)))

(defn move-right! []
  (swap! game-state update :col #(mod (inc %) 5)))


(defn shift-down! []
  (update-column
    (:col @game-state)
    (rotate-right 1
                  (get-column (:col @game-state)
                              (:grid @game-state)))
    (:grid @game-state)))

(defn shift-up! []
  (update-column
    (:col @game-state)
    (rotate-left 1
                 (get-column (:col @game-state)
                             (:grid @game-state)))
    (:grid @game-state)))

(defn shift-left! []
  (update-row
    (:row @game-state)
    (rotate-left 1
                 (get-row (:row @game-state)
                          (:grid @game-state)))))

(defn shift-right! []
  (update-row
    (:row @game-state)
    (rotate-right 1
                  (get-row (:row @game-state)
                           (:grid @game-state)))))

(defn move! [key]
  (case key
    40 ;; down
    (move-down!)
    #_(shift-down!)
    38 ;; up
    (move-up!)
    #_(shift-up!)
    37 ;; left
    (move-left!)
    #_(shift-left!)
    39 ;; right
    (move-right!)
    #_(shift-right!)
    nil))

(defn shift! [key]
  (let [grid (:grid @game-state)]
    (case key
      40 ;; down
      (do
        (shift-down!)
        (check-for-completions grid)
        (check-win))
      38 ;; up
      (do
        (shift-up!)
        (check-for-completions grid)
        (check-win))
      37 ;; left
      (do
        (shift-left!)
        (check-for-completions grid)
        (check-win))
      39 ;; right
      (do
        (shift-right!)
        (check-for-completions grid)
        (check-win))
      nil)))

(defn on-keydown! [e]
  (let [key (.-keyCode e)]
    (if (not= key 32)
      (if (= :move (:cursor-type @game-state))
        (move! key)
        (shift! key))
      (swap! game-state assoc :cursor-type :shift))))

(defn on-keyup! [e]
  (let [key (.-keyCode e)]
    (when (= key 32)
      (swap! game-state assoc :cursor-type :move))))

(defn cell [value is-active?]
  [:div {:class (when is-active?
                  "active-cell")
         :style {:display "flex"
                 :color "white"
                 :background-color (or (color-map value) "#F5F5F5")
                 :width "50px"
                 :height "50px"
                 :align-items "center"
                 :text-align "center"
                 :justify-content "center"}}
   (str value)])

(defn indicator-cell [value]
  [:div {:style {:display "flex"
                 :color "black"
                 :background-color "#F5F5F5"
                 :width "50px"
                 :height "50px"
                 :align-items "center"
                 :text-align "center"
                 :justify-content "center"}}
   (str value)])

(defn scoreboard []
  (let [time (:time @game-state)
        win-total (:win-total @game-state)
        completions (:completions @game-state)]
    [:div
     [:div.subtitle "Time Remaining:" time]
     [:div.subtitle "Completions"]
     (map (fn [[k v]]
            ^{:key k} [:div
                       {:style
                        {:background-color (when (> v win-total)
                                             "green")
                         :color (when (> v win-total)
                                  "white")}}
                       (str k ": " v)])
          completions)]))

(defn game-board []
  (let [state-col (:col @game-state)
        state-row (:row @game-state)
        grid (:grid @game-state)]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :max-width "300px"}}
     [:div {:style {:display "flex"
                    :flex-direction "row"}}
      (map (fn [i]
             (if (= (dec i) state-col)
               ^{:key (str "col-ind-" i)} [indicator-cell "V"]
               ^{:key (str "col-ind-" i)} [indicator-cell nil]))
           (range 6))]
     (map-indexed (fn [i row]
                    ^{:key (str "row-" i)}
                    [:div {:class (str "row-" i)
                           :style {:display "flex"
                                   :flex-direction "row"}}
                     (if (= i state-row)
                       ^{:key (str "row-ind-" i)} [indicator-cell ">"]
                       ^{:key (str "row-ind-" i)} [indicator-cell nil])
                     (map-indexed
                       (fn [j {:keys [id value]}]
                         (let [is-active? (and (= i state-row)
                                               (= j state-col))]
                           ^{:key (str "cell-" id)}
                           [cell value is-active?]))
                       row)])
                  grid)]))

(defn shifter []
  (reagent/with-let
    [_ (.addEventListener js/document "keydown" on-keydown!)
     _ (.addEventListener js/document "keyup" on-keyup!)]
    [:div.container.is-fluid
     [:h1.title "Shifter"]
     (if (:game-over @game-state)
       [:div
        [:h2.title "GAME OVER" (when (= :win (:game-over @game-state)) "-- You win!")]
        [:button {:on-click #(reset! game-state (new-game))} "New Game"]]
       (if (:running? @game-state)
         [:div
          [common/game-timer game-state]
          [game-board]
          [scoreboard]]
         [:div
          [:h2.subtitle "How to play:"]
          [:p "Use the arrow keys to move your cursor. Hold space and use the arrow keys to move rows
          and columns around the cursor. Make rows or columns to get completions.
          Once you have 5 completions for each number before the time runs out, you win!"]
          [:button {:on-click #(swap! game-state assoc :running? true)} "Start Game"]]))]
    (finally
      (.removeEventListener js/document "keydown" on-keydown!)
      (.removeEventListener js/document "keyup" on-keyup!))))