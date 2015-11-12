(ns human.core
  (:use 
  arcadia.core 
  arcadia.hydrate
  hard.core
  hard.animation
  hard.edit
  math.spline)
  (:import [UnityEngine]))

(defn rig [o] 
  (reveal-bones o)
  (form-colliders o))

(defn ->skinned [o]
  (->comp (->go o) UnityEngine.SkinnedMeshRenderer))

(defn shape-count [o]
  (.blendShapeCount (.sharedMesh (->skinned o))))

(defn make-human [_]
  (clear-cloned!)

  (dorun 
    (for [x (range 12)
          z (range 2)
          :let [rider (clone! :humans/rider [(- (* x 2) 40) 4 (- (* z 2) 1)])
                head (child-named rider "infihead")]]
  
  (let [sm (->skinned head)]
    (local-scale! head (rand-vec [0.8 1.2] [0.9 1.2] [1 1]))
    (set! (.color (.material (.GetComponent head UnityEngine.Renderer))) (color (rand-vec 1 1 1)))
    (dorun (for [i (range (shape-count head))]
      (if true
      (.SetBlendShapeWeight sm (int i) (Mathf/Pow (rand 10) 2)))))
    (log (mapv #(.GetBlendShapeWeight sm (int %)) (range 17)))))))




(defn draw-track [_]
  (on-draw-gizmos 
    [(->v3 -40 0 0)(->v3 -20 0 0)(->v3 0 -5 8) (->v3 20 -10 -10) (->v3 40 -25  17) (->v3 60 -30 -10) (->v3  20 -40 30)(->v3  00 -30 20)]
    [(->v3 0 1 0)(->v3 0 1 0)(->v3 1 0 0) (->v3 0 1 0) (->v3 -1 0 0) (->v3 0 -1 0) (->v3 1 0 0)(->v3 0 1 1)]))




(comment 

(make-human nil)

(use 'hard.core)
(import '[PerlinNoise])
(clear-cloned!)
(let [P (PerlinNoise. (rand-int 100))]
  (dorun 
    (for [x (range 100)
        z (range 100)
        y (range 100)
        :let [v (.Noise P (* 0.05 (float x)) (* 0.05 (float y)) (* 0.05 (float z)) )]]
        (if (> (rand) 0.55)
        (if (> 0.3 v 0.29)
          (let [o (clone! :rock_5 (->v3 [x y z]))]
          (local-scale! o (vec (repeatedly 3 #(* 30 v))))
          (rotate! o (rand-vec 360 360 360)))
          ))))
   true
  ))