(ns coaster.world
  (:use 
      arcadia.core 
      seed.core
      hard.core
      hard.mesh
      math.spline
      seed.core)
    (:import [UnityEngine Time Color ]))

(def PN (atom {}))

(def FREE-TILES (atom []))
(def USED-TILES (atom {}))
(def VIEW-POINT (atom nil))

(def COLORS (atom (vec (repeatedly 6 #(rand-vec [0.2 0.9] [0.1 0.8] [0.2 0.7])))))

(def tile-size 200)

(def tile-offsets
  [[-4 -2] [-4 -1] [-4 0] [-4 1] [-4 2] [-3 -3] [-3 -2] [-3 -1] [-3 0] [-3 1] [-3 2] [-3 3] [-2 -4] [-2 -3] [-2 -2] [-2 -1] [-2 0] [-2 1] [-2 2] [-2 3][-2 4] [-1 -4] [-1 -3] [-1 -2] [-1 -1] [-1 0] [-1 1] [-1 2] [-1 3] [-1 4] [0 -4] [0 -3] [0 -2] [0 -1] [0 0] [0 1] [0 2] [0 3] [0 4] [1 -4][1 -3] [1 -2] [1 -1] [1 0] [1 1] [1 2] [1 3] [1 4] [2 -4] [2 -3] [2 -2] [2 -1] [2 0] [2 1] [2 2] [2 3] [2 4] [3 -3] [3 -2] [3 -1][3 0] [3 1] [3 2] [3 3] [4 -2] [4 -1] [4 0] [4 1] [4 2]])


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

(defn terrain-height [x z]
  (* (noise :terrain 
            (* x 0.004) 
            0 
            (* z 0.004)) 200))

(defn draw-tile [o pos]
  (position! o pos)
  (map-mesh-set! o 
  (fn [i v] 
    (->v3 (X v) 
          (* (noise :terrain 
            (* (+ (* (X v) 20)(X pos)) 0.004) 
            0 
            (* (+ (* (Z v) 20)(Z pos)) 0.004)) 10) 
          (Z v))))
  (hill-color o
    (fn [i normal] (color (->vec  (v*  (->v3 (apply v+ (mapv #(v* %2 %1)   (v* normal [1 1 1]) @COLORS))) 0.333))))))

(defn draw-terrain [point]
  (let [point [(int (/ (X point) tile-size))(int (/ (Z point) tile-size))]]
    (when (not= @VIEW-POINT point)
      (reset! VIEW-POINT point)
      (let [in-view (mapv (fn [[x y]] [(+ x (first point)) (+ y (last point))]) tile-offsets)
            unused 
              (filterv 
              #(> (Vector2/Distance 
                (Vector2. (first %) (last %)) 
                (Vector2. (first point) (last point))) 5.0) 
          (keys @USED-TILES))]
        (swap! FREE-TILES (fn [col] (concat col (vals (select-keys @USED-TILES unused)))))
        
        (mapv 
          (fn [point]
            (if-not (get @USED-TILES point)
              (when-let [tile (first @FREE-TILES)]
                (swap! FREE-TILES rest)
                (draw-tile tile (->v3 (v* [(first point) 0 (last point)] tile-size)))
                (swap! USED-TILES #(assoc %  point tile)))))
          in-view)
        (swap! USED-TILES (fn [col] (apply dissoc col unused)))
        true
        ))))


(defn generate-world
  ([seed]
    (seed! seed)
    (generate-world))
  ([]
    (reset! COLORS (vec (repeatedly 6 #(rand-vec [0.3 0.9] [0.4 0.9] [0.4 0.9]))))
    (reset! VIEW-POINT nil)
    (reset! USED-TILES {})
    (reset! PN {:terrain (PerlinNoise. (srand))}) 
    (let [terrain (clone! :Terrain)
          tiles (mapv #(let [o (clone! :grid [0 (+ -99999 %) 0])] (parent! o terrain) o) (range 100))]
      (local-scale! (generate-skydome) (->v3 90000 90000 90000))
      (reset! FREE-TILES tiles)
      
      true)
    (draw-terrain (->v3 0 0 0))))

