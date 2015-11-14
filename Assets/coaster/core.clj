(ns coaster.core
  (:use 
    arcadia.core 
    arcadia.hydrate
    seed.core
    hard.core
    hard.animation
    hard.edit
    math.spline
    human.core)
  (:import [UnityEngine Time]))

(def speed 1.1)
(def interval 0.25)

(def T (atom 0))

(def TRACK (atom [[] []]))

(defn gen-track [n]
  (let [data
    (vec (for [i (range n)
        :let [x (* i 10)
              y (* 2 (if (odd? i) (Mathf/Cos (- i)) (Mathf/Cos  i)))]]
    (->v3 x (* y 9) (* (Mathf/Sin (- y x)) 5))))]
  (reset! TRACK [data (vec (repeatedly n #(->v3 [0 1 0])))])
  true))

(defn maze [n]
  (let [p (->v3 0 0 0)
        data [
    (reduce 
      (fn [col prv] 
        (concat col [(->v3 (v+ (last col)
          (v*  (rand-nth [
              [1 0 0][-1 0 0][1 1 0] [1 -1 0] [0 0 1][0 0 -1][0 -1 1]
              [0 1 1][1 1 1][1 -1 1][1 1 -1][1 1 1]
              [-1 1 0] [-1 -1 0][-1 1 1][-1 -1 1][-1 1 -1][-1 1 1]
              [-1 0 1][1 0 1][-1 0 -1][1 0 -1][1 0 0][1 0 0][-1 0 0][0 0 1][0 0 -1]]) 
            [20 10 20])))]))
      [p]
      (range n))
    (vec (repeat n (->v3 0 1 0)))]]
    (reset! TRACK data)
    true))

(defn make-kart []
  (let [kart (clone! :cart)]
    (mapv 
      (fn [x]
        (let [rider (make-human)]
          (position! rider [x 0 -0.31])
          (rotate! rider [0 180 0])
          (parent! rider kart))) 
      [0.5 -0.5]
      )
    kart))




(defn test-scene [_]
  (gen-track 1000)
  (clear-cloned!)
  (dorun (for [z (range 20)] (position! (make-kart) [0 0 (* z 5)])))
  (let [hook (.AddComponent (clone! :rock_5) hooks.UpdateHook)]
    (set! (.namespaceName hook) "coaster.core")
    (set! (.varName hook) "update-test"))
  (let [hook (.AddComponent (clone! :rock_5) hooks.OnDrawGizmosHook)]
    (set! (.namespaceName hook) "coaster.core")
    (set! (.varName hook) "draw-gizmos")))

(defn update-test [_]
  (swap! T + Time/deltaTime)
  (vec (map-indexed
    (fn [i rider]
      (let [pos (spline (+ (* @T speed) (* i interval)) (first @TRACK))
            next-pos (spline (+ (* @T speed) (* i interval) interval)  (first @TRACK))]
        (position! rider pos)
        (look-at! (->transform rider) next-pos))) 
    (arcadia.core/objects-named "cart") )) 
  
  (look-at! (->transform (the Camera)) (the stable)))

(defn draw-gizmos [_]
  (apply on-draw-gizmos (mapv #(take 18 (drop (int (* @T speed)) %)) @TRACK)))


(test-scene nil)



;(sel! (first (shuffle  
;      (arcadia.core/objects-named "head"))))