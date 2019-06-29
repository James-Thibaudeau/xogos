(ns xogos.events
  (:require
   [re-frame.core :as re-frame]
   [xogos.db :as db]
   [xogos.config :refer [user-key]]
   [xogos.local-storage.utils :as ls]))

(defn save-user-local-storage [user]
  (js/console.log :save-user user)
  (ls/store-ls user-key user))

(defn initialize-db [_ _]
  db/default-db)

(defn set-active-panel [db [_ active-panel]]
  (assoc db :active-panel active-panel))

(defn set-user [db [_ user]]
  (assoc db :user user))

(defn create-user [{:keys [db]} [_ user-name]]
  (let [user {:user-name user-name}]
    {:db (assoc db :user user)
     :save-user-local-storage user}))

(re-frame/reg-fx :save-user-local-storage save-user-local-storage)
(re-frame/reg-event-db ::initialize-db initialize-db)
(re-frame/reg-event-db ::set-active-panel set-active-panel)
(re-frame/reg-event-db ::set-user set-user)
(re-frame/reg-event-fx ::create-user create-user)
