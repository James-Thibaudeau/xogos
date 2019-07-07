(ns xogos.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require
    [secretary.core :as secretary]
    [goog.events :as gevents]
    [goog.history.EventType :as EventType]
    [re-frame.core :as re-frame]
    [xogos.events :as events]))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
            (re-frame/dispatch [::events/set-active-panel :breakout]))
  (defroute "/breakout" []
            (re-frame/dispatch [::events/set-active-panel :breakout]))
  (defroute "/brick-click" []
            (re-frame/dispatch [::events/set-active-panel :brick-click]))
  (defroute "/elevator" []
            (re-frame/dispatch [::events/set-active-panel :elevator]))
  (defroute "/shifter" []
            (re-frame/dispatch [::events/set-active-panel :shifter]))
  (defroute "/tic-tac-toe" []
            (re-frame/dispatch [::events/set-active-panel :shifter]))


  ;; --------------------
  (hook-browser-navigation!))
