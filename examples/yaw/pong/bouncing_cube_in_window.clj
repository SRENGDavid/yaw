
(ns yaw.cube-in-a-box-domino
  "A simple 3D example using a reagent-like approach."
  (:require 
   [clojure.set :as set]
   [yaw.world :as world]
   [yaw.reaction :as react]
   [yaw.render :as render]))

(def +myctrl+ (world/start-universe!))

;;; =====================
;;; The state part
;;; =====================

(react/register-state  ::cube-state nil)

(def init-cube-state
  {:pos [0 0 -5]
   :delta [0.01 -0.015 0]})

;;; =====================
;;; Subscription(s)
;;; =====================

(react/register-subscription 
 ::cube-changed
 (fn [db]
   (::cube-state  db)))

;;; ====================
;;; Event handlers
;;; ====================

(react/register-event
 :react/initialize
 (fn []
   (react/init-state ::cube-state init-cube-state)))

(react/register-event
 :react/frame-update
 (fn []
   ;; (println "[event] frame-update => ::move-cube")
   (react/dispatch [::move-cube])))

(declare update-cube-state)

(react/register-event
 ::move-cube
 (fn []
   ;; (println "[event] ::move-cube -> change-state")
   (react/update-state ::cube-state 
                       (fn [cube-state]
                         (update-cube-state cube-state)))))

(defn inter? [s1 s2]
  (if (not-empty (set/intersection s1 s2))
    true
    false))

;; (inter? #{} #{:a})
;; => false

;; (inter? #{:a} #{:b})
;; => false

;; (inter? #{:a} #{:a :b})
;; => true

(def min-x -3.5)
(def max-x 3.5)
(def min-y -2.5)
(def max-y 2.5)
(def min-z -5)
(def max-z -5)

(defn limit-set [comp coord limit key]
  (if (comp coord limit)
    #{key}
    #{}))

;; (limit-set < 2 3 :underflow)
;; => #{:underflow}

;; (limit-set < 3 2 :underflow)
;; => #{}

(defn bounds-checker [min-x max-x min-y max-y min-z max-z]
  (fn [[x y z]]
    (set/union (limit-set < x min-x :underflow-x)
               (limit-set > x max-x :overflow-x)
               (limit-set < y min-y :underflow-y)
               (limit-set > y max-y :overflow-y)
               (limit-set < z min-z :underflow-z)
               (limit-set > z max-z :overflow-z))))
                       
(def pos-check (bounds-checker min-x max-x min-y max-y min-z max-z))

;; (pos-check [0 0 -7])
;; => #{}

;; (pos-check [-3 0 -7])
;; => #{:underflow-x}

;; (pos-check [-3 3 -7])
;; => #{:overflow-y :underflow-x}

(defn update-delta [checks [dx dy dz]]
  [(if (inter? #{:underflow-x :overflow-x} checks)
     (- dx) dx)
   (if (inter? #{:underflow-y :overflow-y} checks)
     (- dy) dy)
   (if (inter? #{:underflow-z :overflow-z} checks)
     (- dz) dz)])

(defn update-cube-state [{pos :pos
                          delta :delta}]
  (let [checks (pos-check pos)
        delta' (update-delta checks delta)]
    {:pos (mapv + pos delta')
     :delta delta'}))




;;; =====================
;;; The view part
;;; =====================

(defn the-cube
  "Create a cube with its position linked to the `pos` reactive atom."
  [cube-state]
  [:item :test/box {:mesh :mesh/box
                    :pos (:pos @cube-state)
                    :rot [0 0 0]
                    :mat :red
                    :scale 0.3}])

(defn scene []
  [:scene
   [:ambient {:color :white :i 0.4}]
   [:sun {:color :red :i 1 :dir [-1 0 0]}]
   [:light ::light {:color :yellow :pos [0.5 0 -4]}]
   (let [cube-state (react/subscribe +myctrl+ [::cube-changed])]
    [the-cube cube-state])])

;;; =====================
;;; The main part
;;; =====================

(react/activate! +myctrl+ [scene])
;; (react/dispatch :react/initialize)