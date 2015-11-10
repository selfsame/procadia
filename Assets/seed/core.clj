(ns seed.core
  (:use arcadia.core))

(defn seed! [v] (set! UnityEngine.Random/seed (hash v)))

(defn srand 
  ([] UnityEngine.Random/value)
  ([n] (* n (srand))))

(defn srand-int [n] 
  (int (* (srand) n)))

(defn nth-srand [col] 
  (get col (srand-int (count col))))

(defcomponent Seedtest [^int seed]
  (Start [this] 
    (seed! (.seed this))
    (UnityEngine.Debug/Log 
      (str "Seeded: "(srand-int 50)))))

(use 'hard.core)
(import '[PerlinNoise])

(comment 
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
