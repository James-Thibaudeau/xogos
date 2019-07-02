(ns xogos.common.loot)


(def buyable-loot {:wood-hammer 5
                   :bronze-hammer 15
                   :silver-hammer 35
                   :gold-hammer 100})

(def loot-style-map {:uranium {:class "fa-atom"
                               :color "chartreuse"}
                     :bronze-ankh {:class "fa-ankh"
                                   :color "#B08D57"}
                     :silver-ankh {:class "fa-ankh"
                                   :color "silver"}
                     :gold-ankh {:class "fa-ankh"
                                 :color "gold"}
                     :money {:class "fa-money-bill-wave"
                             :color "green"}
                     :bronze-coins {:class "fa-coins"
                                    :color "#B08D57"}
                     :silver-coins {:class "fa-coins"
                                    :color "silver"}
                     :gold-coins {:class "fa-coins"
                                  :color "gold"}
                     :bone {:class "fa-bone"
                            :color "grey"}
                     :wood-hammer {:class "fa-hammer"
                                   :color "brown"}
                     :bronze-hammer {:class "fa-hammer"
                                     :color "#B08D57"}
                     :silver-hammer {:class "fa-hammer"
                                     :color "silver"}
                     :gold-hammer {:class "fa-hammer"
                                   :color "gold"}})


(defn loot-color [key]
  (get-in loot-style-map [key :color]))

(defn loot-class [key]
  (get-in loot-style-map [key :class]))