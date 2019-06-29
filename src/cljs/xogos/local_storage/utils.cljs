(ns xogos.local-storage.utils)

(defn store-ls [k v]
  (->> v
       clj->js
       js/JSON.stringify
       (.setItem (.-localStorage js/window) k)))

(defn get-ls [k]
  (-> js/window
      .-localStorage
      (.getItem k)
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn remove-ls [k]
  (-> js/window
      .-localStorage
      (.removeItem k)))