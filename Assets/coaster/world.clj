(ns coaster.world
  (:use 
      arcadia.core 
      arcadia.hydrate
      seed.core
      hard.core
      hard.animation
      hard.edit
      hard.mesh
      math.spline
      seed.core)
    (:import [UnityEngine Time Color]))

(defn cos [n] (Mathf/Cos n))
(defn sin [n] (Mathf/Sin n))
(defn tan [n] (Mathf/Tan n))

(defn generate-skydome
  ([seed]
    (seed! seed)
    (generate-skydome))
  ([]
    (let [skydome (clone! :skydome)
          [c1 c2 c3 c4 c5 c6] (mapv color (sort #(< (first %1) (first %2)) (repeatedly 6 #(rand-vec [0.2 0.9] [0.1 0.8] [0.2 0.7]))))
          [m1 m2 m3] (vec (shuffle [(srand 30)(srand 24)(srand 13)]))]
      (vertex-colors! skydome 
        (fn [x y z i] 
          (Color/Lerp c5
            (Color/Lerp c1 
              (Color/Lerp c6 c3 (sin (* y m3)))
              (cos (* x m2)))
            (cos (* z m1)))))
      skydome)))

(defn generate-world
  ([seed]
    (seed! seed)
    (generate-world))
  ([]
    
    (let []
      (local-scale! (generate-skydome) (->v3 90000 90000 90000))

      )))

