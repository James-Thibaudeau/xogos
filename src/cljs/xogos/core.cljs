(ns xogos.core
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [xogos.events :as events]
    [xogos.subs :as subs]
    [xogos.routes :as routes]
    [xogos.views :as views]
    [xogos.config :as config]
    [xogos.local-storage.utils :as ls]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn load-user! []
  (when-let [user (ls/get-ls config/user-key)]
    (re-frame/dispatch-sync [::events/set-user user])))

(defn register-on-close []
  (.addEventListener js/window "unload" (fn []
                                          (ls/store-ls
                                            config/user-key
                                            @(re-frame/subscribe [::subs/user])))))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (load-user!)
  (register-on-close)
  (dev-setup)
  (mount-root))
