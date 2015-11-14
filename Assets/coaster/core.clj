(ns coaster.core
  (:use 
    arcadia.core 
    arcadia.hydrate
    seed.core
    hard.core
    hard.animation
    hard.edit
    hard.physics
    math.spline
    human.core
    coaster.world)
  (:import [UnityEngine Time]))

(def speed 1.1)

(def interval 0.2)

(def T (atom 0))

(def TRACK (atom [[] []]))

(defn gen-track [n]
  (let [data
    (vec (for [i (range 500 (+ n 500))
        :let [j (- 120 (* 50 (Mathf/Sin (* i 0.2))))
              x (if (< i 100) 
                  (* j (Mathf/Cos (* i 0.1)) )
                  (* j (Mathf/Cos (* i 0.1)) ))]]
    (->v3 (+ i x) (+ 50 (* 60 (Mathf/Sin (* i 0.5)) (Mathf/Sin (* i 0.025)) )) 
          (+ i (* j (Mathf/Sin (* i 0.13))))
          )))]
  (reset! TRACK [data (vec (repeatedly n #(->v3 [0 1 0])))])
  true))

(defn maze [n]
  (let [p (->v3 0 6 0)
        data [
    (reduce 
      (fn [col prv] 
        (concat col [(->v3 (v+ (last col)
          (v*  (rand-nth [
              [1 0 0][-1 0 0][1 1 0] [1 -1 0] [0 0 1][0 0 -1][0 -1 1]
              [0 1 1][1 1 1][1 -1 1][1 1 -1][1 1 1]
              [-1 1 0] [-1 -1 0][-1 1 1][-1 -1 1][-1 1 -1][-1 1 1]
              [-1 0 1][1 0 1][-1 0 -1][1 0 -1][1 0 0][1 0 0][-1 0 0][0 0 1][0 0 -1]]) 
            [40 20 40])))]))
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
      [0.5 -0.5])
    kart))


(defn gen-scaffold []
  (let [interval 0.6
        len 80
        points 
        (for [i (range len)]
          (spline (* i interval) (first @TRACK)))]
      (reduce 
        (fn [a b] 
          (let [lowest (if (< (Y a) (Y b)) a b)
                rot (look-quat [(X b) 0 (Z b)][(X a) 0 (Z a)])
                dist (Vector3/Distance (->v3 (X a) 0 (Z a)) (->v3 (X b) 0 (Z b)))

                ]
            (doseq [i (take 16 (range (int (/ (Y lowest) dist))))]
              (let [o (clone! (get {0 :girder} i :girder2) (v- [(X a)(Y lowest)(Z a)] [0 (* i dist) 0]))]
                (local-scale! o (->v3 (/ dist 10) (/ dist 10) (/ dist 10)))
                (set!(.rotation (.transform o)) rot)))
            b))
        points)
    ))
 
(defn test-scene [_]
  (gen-track 600)
  (dorun (for [z (range 10)] (position! (make-kart) [0 0 (* z 5)])))
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
   (mapv (comp #(force! %  (- (rand 100) 50) 0 0 ) ->rigidbody) 
      (arcadia.core/objects-named "hand.R")) 
  
  (look-at! (->transform (the Camera)) (->v3 (the cart)))
  )

(defn draw-gizmos [_]
  (comment (apply on-draw-gizmos (mapv #(take 2 (drop (int (* @T speed)) %)) @TRACK)))
  )
 

(clear-cloned!)
(clone! :Camera)
(generate-world)
(test-scene nil)
(gen-scaffold)
(let [seat (first (shuffle (arcadia.core/objects-named "head")))]
  (position! (the Camera)  (v+ (->v3 seat) [0.5 0 -4]))
  (parent! (the Camera) seat)
  (look-at! (->transform (the Camera)) (->v3 (the cart)))
  (sel! (the Camera)))

