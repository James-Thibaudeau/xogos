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

(defn games-panel []
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

(defn user-panel []
  (let [user-name (reagent/atom nil)]
    (fn []
      [:div.container.is-fluid
       [:h1.title "Welcome to Xogos"]
       [:p "Before you can play these amazing games, enter a user name!"]
       [:p "This is a one time signup to create a user local to your browser."]
       [:div
        [:div.field
         [:label {:for "user-name"} "Username"]
         [:div.control
          [:input#user-name.input
           {:value @user-name
            :onChange #(reset! user-name (-> % .-target .-value))}]]]
        [:div.field
         [:button.button.is-primary
          {:on-click #(re-frame/dispatch [::events/create-user @user-name])}
          "Submit"]]]])))


(defn main-panel []
  (let [user (re-frame/subscribe [::subs/user])]
    (fn []
      (if @user
        [games-panel]
        [user-panel]))))