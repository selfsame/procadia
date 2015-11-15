(ns coaster.core
  (:use 
    arcadia.core 
    arcadia.hydrate
    seed.core
    hard.core
    hard.animation
    hard.edit
    hard.mesh
    hard.physics
    math.spline
    human.core
    coaster.world)
  (:import [UnityEngine Time]))

(def speed 1.5)

(def interval 0.16)

(def T (atom 0))

(def TRACK (atom [[] []]))



(defn gen-track [n]
  (let [data
    (vec (for [i (range 1 (+ n 1))
        :let [
              j (- 150 (* 50 (Mathf/Sin (* i 0.2))))
              x (if (< i 100) 
                  (* j (Mathf/Cos (* i 0.1)) )
                  (* j (Mathf/Cos (* i 0.1)) ))
    res (->v3 (+ i x) (+ 50 (* 60 (Mathf/Sin (* i 0.5)) (Mathf/Sin (* i 0.025)) )) 
          (+ i (* j (Mathf/Sin (* i 0.13))))
          )]]
    (V+ res (->v3 0 (+ (* (+ (Mathf/Abs (Z res)) (Mathf/Abs (X res))) 0.3 )
                       (* (noise :terrain (V* res 0.003)) (+ 200 ))) 0))))]
  (reset! TRACK [data (vec (repeatedly n #(->v3 [0 1 0])))])
  true))

(defn gridder [n]
  (let [data
    (vec (for [x (range n) 
               z (range n)]
      (->v3 x (* (noise :terrain (* x 0.04) 0.0 (* z 0.04))  20) z)))]
  (reset! TRACK [data (vec (repeatedly n #(->v3 [0 1 0])))])
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
  (let [holder (clone! :Sphere)
        interval 0.5
        len 200
        points 
        (for [i (range 4 len)]
          (spline (* i interval) (first @TRACK)))]
        (set! (.name holder) "scaffold")
      (reduce 
        (fn [a b] 

          (let [lowest (if (< (Y a) (Y b)) a b)
                rot (look-quat [(X b) 0 (Z b)][(X a) 0 (Z a)])
                dist (Vector3/Distance (->v3 (X a) 0 (Z a)) (->v3 (X b) 0 (Z b)))]

            (doseq [i (take (+ 2 (srand 6)) (range (int (/ (Y lowest) dist))))]
              (let [target (v- [(X a)(Y lowest)(Z a)] [0 (* i dist) 0])]
                (when (or (= i 0) (< 0 (noise :scaffold (->v3 (v* target 0.01)))))
                  (let [o (clone! (get {0 :girder} i :girder2) target)]
                    (local-scale! o (->v3 (/ dist 10) (/ dist 10) (/ dist 10)))
                    (set! (.rotation (.transform o)) rot)
                    (parent! o holder)))))
            b))
        points)))
 
(defn make-train [_]
  (dorun (for [z (range 20)] (position! (make-kart) [0 0 (* z 5)])))
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
   (comment (mapv (comp #(force! %  (- (rand 100) 50) 0 0 ) ->rigidbody) 
      (arcadia.core/objects-named "hand.R"))) 
  (look-at! (->transform (the Camera)) (->v3 (the cart))))

(defn draw-gizmos [_]
  (comment (apply on-draw-gizmos (mapv #(take 2 (drop (int (* @T speed)) %)) @TRACK))
  (set! Gizmos/color (color 1 0 1))
    (dorun (map-indexed #(Gizmos/DrawLine %2 (get (first @TRACK) (inc %1) (->v3 0))) (first @TRACK)))))


(defn setup-game []
  (clear-cloned!)
  (clone! :Camera)
  (generate-world 'joseph)
  (gen-track 1000)
  (gen-scaffold)
  (make-train nil)

  (let [seat (last (shuffle (arcadia.core/objects-named "head")))]
    (position! (the Camera)  (v+ (->v3 seat) [0.5 7 0]))
    (parent! (the Camera) seat)
    (look-at! (->transform (the Camera)) (->v3 (the cart)))
    (sel! (the Camera))))

(setup-game)




 
(do 
(set! (.name (clone! :Sphere)) "Terrain")
(dorun (for [x (range -5 5) z (range -5 5)]
  (let [tile (clone! :tile (v* [x 0 z] 100))]

(parent! tile (the Terrain))
(map-mesh-set! tile 
  (fn [i v] 
    (->v3 (X v) 
          (* (noise :terrain (V* (V+ v 
            (->v3 (v* [x 0 z] 10)))
             0.1)) 70) 
          (Z v))))
(gradiate tile)
))) 
(local-scale! (the Terrain) [5 1 5])
true)
