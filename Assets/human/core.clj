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

(defn make-human []
  (let [rider (clone! :humans/stable)
        head (child-named rider "infihead")
        sm (->skinned head)
        skin-color (color (rand-vec 1 1 1))]
    ;(local-scale! head (rand-vec [0.8 1.2] [0.9 1.2] [1 1]))
    (mapv #(set! (.color (.material (.GetComponent % UnityEngine.Renderer))) skin-color)
      [head (child-named rider "body-mesh")
      (child-named rider "arm-mesh")])
    (dorun (for [i (range (shape-count head))]
      (if true
      (.SetBlendShapeWeight sm (int i) (Mathf/Pow (rand 10) 2)))))
    rider))





(comment 

(make-human)

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