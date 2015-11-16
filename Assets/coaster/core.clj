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

(def track-grow-rate 30) ;; grow track every n frames
(def track-grow-size 100) ;; add n nodes every track growth

(def speed 1.5)

(def interval 0.16)

(def T (atom 0))

; (def TRACK (atom [[] []]))

(def track-positions (atom []))
(def track-normals (atom []))

(defn next-track-point [i]
  (let [i* (* i 0.01)
        radius (+ 150 i*)
        j (* radius (Mathf/Sin (* i 0.2)))
        x (* (* 0.01 j) radius (Mathf/Cos (* i 0.1)) )
        res (->v3 (+ x (* radius (Mathf/Cos (* i* i* 3))))
                  (+ 50 (* radius
                           (Mathf/Sin (* i 0.5))
                           (Mathf/Sin (* i 0.025)) )) 
                  (+ i (* radius (Mathf/Sin (* i* 2))))
                  )]
    (V+ res (->v3 0
                  (+ (* (+ (Z res) (X res)) 0.3)
                     (* (noise :terrain (V* res 0.003)) (+ 200 )))
                  0))))
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
  (reset! track-positions data)
  (reset! track-normals (vec (repeatedly n #(->v3 [0 1 0]))))
  true))

(defn gridder [n]
  (let [data
    (vec (for [x (range n) 
               z (range n)]
      (->v3 x (* (noise :terrain (* x 0.04) 0.0 (* z 0.04))  20) z)))]
  (reset! track-positions data)
  (reset! track-normals (vec (repeatedly n #(->v3 [0 1 0]))))
  true))





(defn make-kart []
  (let [kart (clone! :cart)]
    (mapv 
      (fn [x]
        (let [rider (if (< 0.25 (rand))
                      (make-human)
                      (clone! :empty-seat))]
          (position! rider [x 0 -0.31])
          (rotate! rider [0 180 0])
          (parent! rider kart))) 
      [0.5 -0.5])
    kart))

(defn gen-scaffold [positions]
  (let [holder (clone! :Sphere (->v3 [0 0 0]))
        len (count positions)
        points positions]
        (set! (.name holder) "scaffold")
      (reduce 
        (fn [a b] 

          (let [lowest (if (< (Y a) (Y b)) a b)
                rot (look-quat [(X b) 0 (Z b)][(X a) 0 (Z a)])
                dist (Vector3/Distance (->v3 (X a) 0 (Z a)) (->v3 (X b) 0 (Z b)))]

            (doseq [i (take (+ 2 (srand 4)) (range (int (/ (Y lowest) dist))))]
              (let [target (v- [(X a)(Y lowest)(Z a)] [0 (* i dist) 0])]
                (when (or (= i 0) (< -0.95 (noise :scaffold (->v3 (v* target 0.01)))))
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
      (let [pos (spline (+ (* @T speed) (* i interval)) @track-positions)
            next-pos (spline (+ (* @T speed) (* i interval) interval)  @track-positions)]
        (position! rider (V+ pos (->v3 [0 5 0])))
        (look-at! (->transform rider) (V+ next-pos (->v3 [0 5 0]))))) 
    (arcadia.core/objects-named "cart") ))
   (comment (mapv (comp #(force! %  (- (rand 100) 50) 0 0 ) ->rigidbody) 
      (arcadia.core/objects-named "hand.R"))) 
  (look-at! (->transform (the Camera)) (->v3 (the cart))))

(defn draw-whole-track [go]
  (doseq [[a b] (partition 2 1 @track-positions)]
    (Gizmos/DrawSphere a 1)
    (Gizmos/DrawLine a b)
    ))

(defn draw-gizmos [_]
  (comment (apply on-draw-gizmos (mapv #(take 2 (drop (int (* @T speed)) %)) @track-positions @track-normals))
  (set! Gizmos/color (color 1 0 1))
    (dorun (map-indexed #(Gizmos/DrawLine %2 (get @track-positions (inc %1) (->v3 0))) @track-positions))))


(defn grow-track! [n]
  (let [old-size (count @track-positions)]
    (dotimes [i n]
      (swap! track-positions conj (next-track-point (+ old-size i)))
      (swap! track-normals conj (->v3 [0 1 0])))
    (gen-scaffold (take n (drop old-size @track-positions)))))

(defn setup-game []
  (clear-cloned!)
  (clone! :Camera)
  (generate-world (rand))
  (grow-track! 100)
  (make-train nil)

  (let [seat (last (shuffle (arcadia.core/objects-named "empty-seat")))]
    (position! (the Camera)  (v+ (->v3 seat) [0 1.2 0]))
    (parent! (the Camera) seat)
    (look-at! (->transform (the Camera)) (->v3 (the cart)))
    (sel! (the Camera))))

(defn test-scene [go]
  (setup-game))

(comment
  (setup-game))

(defn track-grower [go]
  (if (zero? (mod Time/frameCount track-grow-rate))
    (grow-track! track-grow-size)))
 
