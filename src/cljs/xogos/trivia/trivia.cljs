(ns xogos.trivia.trivia
  (:require [reagent.core :as reagent]))

(def questions {:question-1 {:question "How many provinces in Canada?"
                             :correct :a
                             :answers {:a "10"
                                       :b "12"
                                       :c "13"
                                       :d "States"}}})

(defn is-correct? [guess correct-answer]
  (= guess correct-answer))

(defn question [question]
  (let [answered-right? (reagent/atom false)]
    (fn [question]
      [:div
       [:h2 (:question question)]
       (into [:div {:style {:background-color (when @answered-right? "green")}}]
             (map (fn [[k v]]
                    [:button
                     {:on-click #(reset! answered-right? (is-correct? k (:correct question)))}
                     (str k ": " v) ])
                  (:answers question)))])))


(defn trivia []
  [:div.container.is-fluid
   [:h1.title "Trivia Game"]
   [:div
    (map (fn [[q-key q-info]]
           [question q-info])
          questions)]])