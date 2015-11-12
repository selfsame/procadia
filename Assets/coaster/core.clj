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

(def speed 1.0)

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


(defn test-scene []
  (gen-track 160)
  (clear-cloned!)
  (clone! :Sphere)
  (dorun (for [z (range 40)] (position! (make-human) [0 0 (* z 2)])))
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
      (let [pos (spline (+ (* @T speed) (* i 0.12)) (first @TRACK))
            next-pos (spline (+ (* @T speed) (* i 0.12) 0.12)  (first @TRACK))]
        (position! rider pos)
        (look-at! (->transform rider) next-pos))) 
    (arcadia.core/objects-named "rider") )))

(defn draw-gizmos [_]
  (apply on-draw-gizmos (mapv #(take 12 (drop (int (* @T speed)) %)) @TRACK)))


;(test-scene)
