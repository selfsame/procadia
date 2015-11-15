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
    (:import [UnityEngine Time Color ]))

(def PN (atom {}))

(def FREE-TILES (atom []))
(def USED-TILES (atom {}))

(defn noise 
  ([k v] (cond (vector3? v) (noise k (.x v) (.y v) (.z v))
               (number? v) (noise k v 0.0 0.0)))
  ([k x y] (noise k x y 0))
  ([k x y z]
  (let [pn (or (get @PN k) 
               (get (swap! PN assoc k (PerlinNoise. (srand))) k))]
    (.Noise pn (float x) (float y) (float z)))))

(defn cos [n] (Mathf/Cos n))
(defn sin [n] (Mathf/Sin n))
(defn tan [n] (Mathf/Tan n))


(defn gradiate [o] 
  (let [skydome o
          [c1 c2 c3 c4 c5 c6] (mapv color (sort #(< (first %1) (first %2)) (repeatedly 6 #(rand-vec [0.2 0.9] [0.1 0.8] [0.2 0.7]))))
          [m1 m2 m3] (vec (shuffle [(srand 30)(srand 24)(srand 13)]))]
      (vertex-colors! skydome 
        (fn [x y z i] 
          (Color/Lerp c5
            (Color/Lerp c1 
              (Color/Lerp c6 c3 (sin (* y m3)))
              (cos (* x m2)))
            (cos (* z m1)))))
      o))

(defn generate-skydome
  ([seed]
    (seed! seed)
    (generate-skydome))
  ([] (gradiate (clone! :skydome))))

(defn generate-world
  ([seed]
    (seed! seed)
    (generate-world))
  ([]
    (reset! PN {:terrain (PerlinNoise. (srand))}) 
    (let [terrain (clone! :Terrain)
          tiles (mapv #(parent! (clone! :tile [0 (+ -99999 %) 0]) terrain) (range 100))]
      (local-scale! (generate-skydome) (->v3 90000 90000 90000))
      
      )))


(defn draw-terrain [point]
  (let [tiles (children (the Terrain))
        outer (remove #(< (Vector3/Distance (->v3 %) point) 4000.0) tiles)
    ]
    (mapv #(position! % [0 -10000 0]) outer)))








(comment 


(do (clear-cloned!)
(generate-world))

(draw-terrain (->v3 0 0 0))



(map-mesh-set! tile 
  (fn [i v] 
    (->v3 (X v) 
          (* (noise :terrain (V* (V+ v 
            (->v3 (v* [x 0 z] 10)))
             0.1)) 70) 
          (Z v))))






  )