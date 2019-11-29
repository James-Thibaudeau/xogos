(ns xogos.views
  (:require
    [clojure.string :as s]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [xogos.events :as events]
    [xogos.subs :as subs]
    [xogos.store.views :as sv]
    [xogos.brick-click.brick-click :as bc]
    [xogos.elevator.elevator :as elevator]
    [xogos.shifter.shifter :as shifter]
    [xogos.tic-tac-toe.tic-tac-toe :as ttt]
    [xogos.breakout.breakout :as bo]
    [xogos.trivia.trivia :as trivia]
    [xogos.card-battle.card-battle :as card-battle]))

(def games [:card-battle
            :trivia
            :breakout
            :brick-click
            :shifter
            :elevator
            :tic-tac-toe])

(defn nav-bar [state]
  [:nav.navbar
   [:div.container
    [:div.navbar-brand
     [:i {:class "fas fa-gamepad fa-3x"}]
     [:span.navbar-burger.burger
      {:on-click #(swap! state update :menu-open not)}
      [:span]
      [:span]
      [:span]]]
    [:div.navbar-menu {:class (when (:menu-open @state) "is-active")}
     [:div.navbar-end
      [:a.navbar-item {:on-click #(swap! state update :store-open not)} "Store"]
      (map (fn [g]
             ^{:key g} [:a.navbar-item
                        {:on-click #(re-frame/dispatch [::events/set-active-panel g])}
                        (s/capitalize (name g))])
           games)]]]])

(defn games-panel []
  (let [game (re-frame/subscribe [::subs/active-panel])
        state (reagent/atom {:store-open nil
                             :menu-open nil})]
    (fn []
      [:div.container.is-fluid
       [nav-bar state]
       (case @game
         :card-battle
         [card-battle/card-battle]
         :trivia
         [trivia/trivia]
         :breakout
         [bo/breakout]
         :brick-click
         [bc/brick-click]
         :shifter
         [shifter/shifter]
         :elevator
         [elevator/elevator]
         :tic-tac-toe
         [ttt/tic-tac-toe])
       [sv/store (:store-open @state) #(swap! state assoc :store-open nil)]])))

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