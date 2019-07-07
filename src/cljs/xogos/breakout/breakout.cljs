(ns xogos.breakout.breakout
  (:require [reagent.core :as reagent]
            [reagent.ratom :as ratom]
            [clojure.string :as s]))

(def brick-data {:width 75
                 :height 20
                 :padding 10
                 :offset-top 30
                 :offset-left 30})

(def color-map {1 "#0099ff"
                2 "#00cc33"
                3 "#bb3598"
                4 "#644117"
                5 "#ff7518"})

(defn brick [x y color]
  {:id (random-uuid)
   :x x
   :y y
   :color color
   :width 75
   :height 20})

(defn brick-pos [col width padding offset]
  (+ offset (* col (+ width padding))))

(defn create-bricks [rows cols]
  (mapv
    (fn [row]
      (mapv
        (fn [col]
          (let [x (brick-pos col 75 10 30)
                y (brick-pos row 20 10 30)
                color (get color-map (inc row))]
            (brick x y color)))
        (range cols)))
    (range rows)))

(def initial-game-state {:ball {:x 200
                                :y 200
                                :r 10
                                :dx 3
                                :dy 4}
                         :bricks (create-bricks 3 5)
                         :paddle {:pw 75
                                  :ph 10
                                  :px 200
                                  :py 310}
                         :running? false
                         :game-over? false
                         :right-pressed? false
                         :left-pressed? false})

(def game-state (reagent/atom initial-game-state))

(defn start-game! []
  (swap! game-state assoc :running? true))

(defn reset-game! []
  (reset! game-state initial-game-state))

(defn move-paddle [right-pressed? left-pressed? px pw cw]
  (if (and right-pressed? (< px (- cw pw)))
    (+ px 7)
    (if (and left-pressed? (> px 0))
      (- px 7)
      px)))

#_(defn paddle-collide? [x y r px pw ch]
  (and (>= (+ y r) (- ch r))
       (>= x px)
       (<= x (+ px pw))))


(defn wall-collide? [x r cw]
  (or (>= x (- cw r))
      (>= r x)))

(defn ceil-collide? [y r]
  (<= y r))

(defn wall-collision-detection [{:keys [x y r dx dy] :as ball} cw]
  (assoc ball
    :dx (if (wall-collide? x r cw) (- dx) dx)
    :dy (if (ceil-collide? y r) (- dy) dy)))

#_(defn brick-collide? [cx cy r bx by bw bh]
  (and (or (>= (+ cx r) bx) ;; right edge of ball/left edge of box
           (<= (- cx r) (+ bx bw))) ;; left edge of ball/right edge of box
       (or (>= (+ cy r) by) ;; bottom edge of ball/top edge of box
           (<= (- cy r) (+ by bh))))) ;;

(defn rectangle-collide? [cx cy r bx by bw bh]
  (let [right-edge (+ bx bw)
        bottom-edge (+ by bh)
        x-pos (if (< cx bx)
                bx
                (if (> cx right-edge)
                  right-edge
                  cx))
        y-pos (if (< cy by)
                by
                (if (> cy bottom-edge)
                  bottom-edge
                  cy))
        distance (Math/sqrt (+ (Math/pow (- cx x-pos) 2)
                               (Math/pow (- cy y-pos) 2)))]
    (<= distance r)))


(defn paddle-collision-detection [{:keys [x y r dy] :as ball}
                                  {:keys [pw px py ph]}
                                  ch]
  (assoc ball
    :dy (if (rectangle-collide? x y r px py pw ph) (- dy) dy)))

#_(defn destroy-bricks-on-collision [ball bricks]
  (mapv
    (fn [row]
      (mapv
        (fn [{:keys [x y height width visible?] :as brick}]
          (if (and visible?
                   (rectangle-collide?
                     (:x ball) (:y ball) (:r ball) x y width height))
            (assoc brick :visible? false)
            brick))
        row))
    bricks))

(defn destroy-bricks-on-collision [ball bricks]
  (reduce (fn [new-bricks row]
            (conj new-bricks
              (remove (fn [{:keys [x y height width]}]
                        (rectangle-collide?
                          (:x ball) (:y ball) (:r ball) x y width height))
                      row)))
          [] bricks))

(defn brick-collision? [ball bricks]
  (some (fn [{:keys [x y height width]}]
          (rectangle-collide?
            (:x ball) (:y ball) (:r ball) x y width height))
        (flatten bricks)))

(defn brick-collision-detection [{:keys [dx dy] :as ball} bricks]
  (if (brick-collision? ball bricks)
    (assoc ball
      :dy (- dx)
      :dx (- dy))
    ball))

(defn ball-lost? [y ch]
  (> y ch))

(defn all-bricks-destroyed? [bricks]
  (= 0 (count (flatten bricks))))

(defn move-ball [{:keys [x y dx dy] :as ball}]
  (assoc ball :x (+ x dx) :y (+ y dy)))

(defn update-game-state! []
  (let [current-state @game-state]
    (swap! game-state merge
           (as-> current-state state
                 (assoc state
                   :ball (wall-collision-detection (:ball state) 480))
                 (assoc state
                   :ball (paddle-collision-detection
                           (:ball state)
                           (:paddle state)
                           320))
                 (assoc state :ball (brick-collision-detection
                                      (:ball state) (:bricks state)))
                 (assoc state :bricks (destroy-bricks-on-collision
                                        (:ball state) (:bricks state)))
                 (assoc state :ball (move-ball (:ball state)))
                 (assoc-in state
                           [:paddle :px]
                           (move-paddle (:right-pressed? state)
                                        (:left-pressed? state)
                                        (:px (:paddle state))
                                        (:pw (:paddle state))
                                        480))
                 (assoc state
                   :game-over?
                   (if (ball-lost?
                         (:y (:ball state))
                         320)
                     :lose
                     (if (all-bricks-destroyed? (:bricks state))
                       :win
                       (:game-over? state))))))))

(defn draw-brick [ctx {:keys [x y width height color]}]
  (aset ctx "fillStyle" color)
  (.fillRect ctx x y width height))

(defn draw-bricks [ctx bricks]
  (doseq [brick (flatten bricks)]
    (draw-brick ctx brick)))

(defn draw-ball [ctx r x y]
  (aset ctx "fillStyle" (get color-map 5))
  (.beginPath ctx)
  (.arc ctx x y r 0 (* js/Math.PI 2))
  (.fill ctx)
  (.closePath ctx))

(defn draw-paddle [ctx x y width height]
  (aset ctx "fillStyle" (get color-map 1))
  (.fillRect ctx x y width height))

(defn render! [ctx]
  (let [ball (:ball @game-state)
        paddle (:paddle @game-state)]
    (.clearRect ctx 0 0 480 320)
    (draw-bricks ctx (:bricks @game-state))
    (draw-paddle ctx (:px paddle) (:py paddle) (:pw paddle) (:ph paddle))
    (draw-ball ctx (:r ball) (:x ball) (:y ball))))

(defn animate-fn [render-fn]
  (fn animate! []
    (when (and (:running? @game-state)
               (not (:game-over? @game-state)))
      (update-game-state!)
      (render-fn)
      (.requestAnimationFrame js/window animate!))))

(defn on-keydown! [e]
  (case (.-keyCode e)
    37 ;; left
    (swap! game-state assoc :left-pressed? true)
    39 ;; right
    (swap! game-state assoc :right-pressed? true)
    nil))

(defn on-keyup! [e]
  (case (.-keyCode e)
    37 ;; left
    (swap! game-state assoc :left-pressed? false)
    39 ;; right
    (swap! game-state assoc :right-pressed? false)
    nil))

(defn game-canvas []
  (reagent/create-class
    {:componentDidMount
     (fn []
       (let
         [canvas (.getElementById js/document "breakout")
          ctx (.getContext canvas "2d")
          draw-fn #(render! ctx)
          animate! (animate-fn draw-fn)]
         (.addEventListener js/document "keydown" on-keydown!)
         (.addEventListener js/document "keyup" on-keyup!)
         (animate!)))
     :component-will-unmount
     (fn []
       (.removeEventListener js/document "keydown" on-keydown!)
       (.removeEventListener js/document "keyup" on-keyup!))
     :reagent-render
     (fn []
       [:canvas
        {:id "breakout"
         :width 480
         :height 320
         :style {:border "1px solid black"}}])}))

(defn breakout []
  (let [running? (ratom/make-reaction #(:running? @game-state))
        game-over? (ratom/make-reaction #(:game-over? @game-state))]
    (fn []
      (js/console.log :render-main)
      [:div.container
       [:h1.title "Breakout"]
       (if @running?
         (if-not @game-over?
           [game-canvas]
           [:div (str "GAME OVER - You " (s/capitalize (name @game-over?)))
            [:button {:on-click #(do
                                   (reset-game!)
                                   (start-game!))} "Play Again"]])
         [:button {:on-click start-game!} "Start Game"])])))
