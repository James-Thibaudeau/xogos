(ns xogos.store.views
  (:require
    [reagent.core :as reagent]
    [bulma-cljs.core :as b]
    [xogos.common.loot :as loot]))

(def buyable-loot {:wood-hammer 5
                   :bronze-hammer 15
                   :silver-hammer 35
                   :gold-hammer 100})

(def store-state (reagent/atom {:cart {}}))

(defn update-store-state! [state]
  (swap! store-state merge state))

(defn total-cost-cart [items price-list]
  (reduce
    (fn [prev [k v]]
      (+ prev (* v (k price-list))))
    0
    items))

(defn add-to-cart [item cart]
  (update cart item inc))

(defn remove-from-cart [item cart]
  (if (> (item cart) 1)
    (update cart item dec)
    (dissoc cart item)))

(defn add-to-cart! [item state]
  (-> state
      (update :cart #(add-to-cart item %))
      update-store-state!))

(defn remove-from-cart! [item state]
  (-> state
      (update :cart #(remove-from-cart item %))
      update-store-state!))

(defn store-item [k v]
  [:div.columns
   [:div.column
    [:i {:class ["fas" (loot/loot-class k) "fa-2x"]
         :style {:color (loot/loot-color k)}}]]
   [:div.column
    [:div (get buyable-loot k)]]])

(defn store-inventory [items]
  [:div
   [:h3.subtitle "Store"]
   (map (fn [[k v]]
          ^{:key k} [store-item k v])
        items)])

(defn store [active? close-fn]
  [b/modal active? close-fn
   [:div.modal-card
    [:header.modal-card-head
     [:p.modal-card-title "Store"]]
    [:section.modal-card-body
     [store-inventory buyable-loot]]
    [:footer.modal-card-foot
     [:button.button.is-success "Buy"]]]])