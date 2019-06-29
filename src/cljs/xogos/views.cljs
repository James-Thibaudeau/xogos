(ns xogos.views
  (:require
    [clojure.string :as s]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [xogos.events :as events]
    [xogos.subs :as subs]
    [xogos.brick-click.brick-click :as bc]
    [xogos.elevator.elevator :as elevator]
    [xogos.shifter.shifter :as shifter]
    [xogos.tic-tac-toe.tic-tac-toe :as ttt]))

(def games [:brick-click :shifter :elevator :tic-tac-toe])

(defn nav-bar []
  (let [menu-open? (reagent/atom false)]
    (fn []
      [:nav.navbar
       [:div.container
        [:div.navbar-brand
         [:i {:class "fas fa-gamepad fa-3x"}]
         [:span.navbar-burger.burger
          {:on-click #(swap! menu-open? not)}
          [:span]
          [:span]
          [:span]]]
        [:div.navbar-menu {:class (when @menu-open? "is-active")}
         [:div.navbar-end
          (map (fn [g]
                 ^{:key g} [:a.navbar-item
                            {:on-click #(re-frame/dispatch [::events/set-active-panel g])}
                            (s/capitalize (name g))])
               games)]]]])))


(defn main-panel []
  (let [game (re-frame/subscribe [::subs/active-panel])]
    (fn []
      [:div.container.is-fluid
       [nav-bar]
       (case @game
         :brick-click
         [bc/brick-click]
         :shifter
         [shifter/shifter]
         :elevator
         [elevator/elevator]
         :tic-tac-toe
         [ttt/tic-tac-toe])])))
