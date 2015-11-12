(ns coaster.core
  (:use 
    arcadia.core 
    arcadia.hydrate
    hard.core
    hard.animation
    hard.edit
    math.spline
    human.core)
  (:import [UnityEngine Time]))

(def speed 0.8)
(def interval 0.1)

(def T (atom 0))

(def TRACK (atom [[] []]))

(defn gen-track [n]
  (let [data
    (vec (for [i (range n)
        :let [x (* i 10)
              y (* 2 (if (odd? i) (Mathf/Cos (- i)) (Mathf/Cos  i)))]]
    (->v3 x (* y 9) (* (Mathf/Sin (- y x)) 5))))]
  (reset! TRACK [data (vec (repeat n (->v3 0 1 0)))])
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
            [10 10 10])))]))
      [p]
      (range n))
    (vec (repeat n (->v3 0 1 0)))]]
    (reset! TRACK data)
    true))

(defn test-scene [_]
  (gen-track 300)
  (clear-cloned!)
  ;(mapv #(clone! :Sphere (->v3 % 0 0)) (range 30))
  (dorun (for [z (range 40)] (position! (make-human)     [0 0 (* z 2)])))
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
    (arcadia.core/objects-named "stable") ))
  
  (look-at! (->transform (the Camera)) (the stable)))

(defn draw-gizmos [_]
  (apply on-draw-gizmos (mapv #(take 18 (drop (int (* @T speed)) %)) @TRACK)))


(test-scene nil)
