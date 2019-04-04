
(ns yaw.pong.moving-cube
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

(react/register-state ::pad-state nil)

(def init-cube-state
  {:pos [0 0 -5]
   :delta [0.01 0 0]})

(def init-pad-state
  {:pos [2 0 -5]})

;;; =====================
;;; Subscription(s)
;;; =====================

(react/register-subscription 
 ::cube-changed
 (fn [db]
   (::cube-state  db)))

(react/register-subscription 
 ::pad-changed
 (fn [db]
   (::pad-state  db)))
   

;;; ====================
;;; Event handlers
;;; ====================

(react/register-event
 :react/initialize
 (fn []
   (react/init-state ::cube-state init-cube-state)
   (react/init-state ::pad-state init-pad-state)))

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


(defn update-cube-state [{pos :pos
                          delta :delta}]
    {:pos (mapv + pos delta)
     :delta delta})




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

(defn the-pad
  [pad-state]
  [:item :test/box3 {:mesh :mesh/box
                    :pos (:pos @pad-state)
                    :rot [0 0 0]
                    :mat :red
                    :scale 1}])

(defn scene []
  [:scene
   [:ambient {:color :white :i 0.4}]
   [:sun {:color :red :i 1 :dir [-1 0 0]}]
   [:light ::light {:color :yellow :pos [0.5 0 -4]}]
   (let [cube-state (react/subscribe +myctrl+ [::cube-changed])]
    [the-cube cube-state])
   (let [pad-state (react/subscribe +myctrl+ [::pad-changed])]
    [the-pad pad-state])])

;;; =====================
;;; The main part
;;; =====================

(react/activate! +myctrl+ [scene])
;; (react/dispatch :react/initialize)