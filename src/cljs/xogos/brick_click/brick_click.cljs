(ns xogos.brick-click.brick-click
  (:require
    [reagent.core :as reagent]
    [cljsjs.howler]
    [xogos.common.common :as common]
    [xogos.common.loot :as loot]))

(declare game-state)

(def break-sound (new js/Howl #js{:src #js["sound/break-brick.mp3"]}))
(def loot-sound (new js/Howl #js{:src #js["sound/loot.mp3"]}))
(def level-up (new js/Howl #js{:src #js["sound/levelup.mp3"]}))

(defn toggle-sound []
  (swap! game-state update :sound? not))

(defn play-sound! [state sound]
  (when (:sound? state)
    (.play sound)))

(def minable-loot [:bone :uranium :bronze-ankh :silver-ankh
                   :gold-ankh :money :bronze-coins :silver-coins])

(defn random-loot []
  (when (rand-nth [nil nil true])
    {:type (rand-nth minable-loot)
     :value 1}))

(defn brick [_ _]
  {:id (random-uuid)
   :clicks-left 1
   :loot (random-loot)})

(def game-state (reagent/atom {:grid (common/make-grid 10 10 brick)
                               :level 1
                               :inventory {}
                               :sound? true}))

(defn level-complete? [grid]
  (every?
    (fn [row]
      (every? #(and
                 (= (:clicks-left %) 0)
                 (nil? (:loot %)))
              row))
    grid))

(defn get-brick [x y]
  (get-in @game-state [:grid x y]))

(defn new-grid [state]
  (assoc state :grid (common/make-grid 10 10 brick)))

(defn inc-level [state]
  (update state :level inc))

(defn dec-clicks [state x y]
  (update-in state [:grid x y :clicks-left] dec))

(defn loot-brick [state x y]
  (update-in state [:grid x y :loot] #(identity nil)))

(defn update-inventory [state key value]
  (update-in state [:inventory key] #(+ % value)))

(defn update-game-state! [new-state]
  (swap! game-state merge new-state))

(defn new-level! [state]
  (play-sound! state level-up)
  (-> state 
      new-grid
      inc-level
      update-game-state!))

(defn destroy-brick! [state x y]
  (play-sound! state break-sound)
  (-> state
      (dec-clicks x y)
      update-game-state!))

(defn loot-brick! [state type value x y]
  (play-sound! state loot-sound)
  (-> state
      (update-inventory type value)
      (loot-brick x y)
      update-game-state!))

(defn click-brick [x y]
  (let [{:keys [clicks-left loot]} (get-brick x y)
        {:keys [type value]} loot]
    (if (> clicks-left 0)
      (destroy-brick! @game-state x y)
      (when loot
        (loot-brick! @game-state type value x y)))
    (when (level-complete? (:grid @game-state))
      (new-level! @game-state))))

(defn brick-cell [clicks-left loot x y]
  [:div {:on-click #(click-brick x y)
         :style {:display "flex"
                 :flex "0 0 10%"
                 :position "relative"
                 :color "black"
                 :background-color (if (> clicks-left 0) "brown" "#f5f5f5")
                 :border "2px solid white"
                 :align-items "center"
                 :text-align "center"
                 :justify-content "center"}}
   (if (> clicks-left 0)
     [:img {:src "img/brick.svg"}]
     (when (:value loot)
       [:i {:class ["fas" (loot/loot-class (:type loot)) "fa-2x"]
            :style {:color (loot/loot-color (:type loot))}}]))])

(defn inventory-item [k v]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "end"}}
   [:div
    [:i {:class ["fas" (loot/loot-class k) "fa-2x"]
         :style {:color (loot/loot-color k)}}]]
   [:div {:style {:flex "1"
                  :text-align "center"}}
    [:span "x" v]]])

(defn inventory [items]
  [:div
   [:h3.subtitle "Inventory"]
   (map (fn [[k v]]
          ^{:key k} [inventory-item k v])
        items)])

(defn game-board [grid]
  [:div {:style {:display "flex"
                 :flex-wrap "wrap"
                 :flex-direction "column"
                 :justify-content "space-around"
                 :height "50vw"
                 :width "50vw"}}
   (map-indexed (fn [i row]
                  ^{:key (str "row-" i)}
                  [:div {:class (str "row-" i)
                         :style {:display "flex"
                                 :flex "1"
                                 :flex-direction "row"}}
                   (map-indexed
                     (fn [j {:keys [id clicks-left loot]}]
                       ^{:key (str "brick" id)} [brick-cell clicks-left loot i j])
                     row)])
                grid)])

(defn brick-click []
  [:div.container.is-fluid {:style {:user-select "none"}}
   [:div {:style {:display "flex"
                  :flex-direction "row"
                  :align-content "center"
                  :justify-content "center"}}
    [:h2.title "BrickClick"]
    [:div {:style {:flex "1"
                   :margin-left "1rem"}
           :on-click toggle-sound}
     [:i {:class ["fas" (if (:sound? @game-state) "fa-volume-up" "fa-volume-mute")]}]]]
   [:h2.subtitle "Level: " (:level @game-state)]
   [:div.columns
    [:div.column
     [game-board (:grid @game-state)]]
    [:div.column
     [inventory (:inventory @game-state)]]]])