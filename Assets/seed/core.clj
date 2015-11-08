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