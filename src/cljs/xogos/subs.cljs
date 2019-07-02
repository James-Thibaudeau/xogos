(ns xogos.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub ::active-panel :active-panel)
(re-frame/reg-sub ::user :user)
(re-frame/reg-sub ::inventory :inventory)

