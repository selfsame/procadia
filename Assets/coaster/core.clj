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

(def buffer 50)

(def speed 2)

(def interval 0.16)

(def T (atom 0))


(def SCAFFOLDS (atom '()))

(def track-positions (atom []))
(def track-normals (atom []))

(defn next-track-point [i]
  (let [i* (* i 0.01)
        j (- 150 (* i 0.1 (Mathf/Sin (* i 0.2))))
        x (* j (* 0.1 i*) (Mathf/Cos (* i 0.1)) )
        res (->v3 (+ i x)
                  (+ 50 (* 10 i* (Mathf/Sin (* i 0.5)) (Mathf/Sin (* i 0.025)) )) 
                  (+ i (* j (Mathf/Sin (* i* 0.13))))
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





(defn make-kart []
  (let [kart (clone! :cart)]
    (mapv 
      (fn [x]
        (let [rider (if (< 0.25 (rand))
                      (make-human)
                      (GameObject. "empty-seat"))]
          (position! rider [x 0 -0.31])
          (rotate! rider [0 180 0])
          (parent! rider kart))) 
      [0.5 -0.5])
    kart))

(defn gen-scaffold [positions]
  (let [len (count positions)
        points positions]
      (reduce 
        (fn [a b] 

          (let [lowest (if (< (Y a) (Y b)) a b)
                rot (look-quat [(X b) 0 (Z b)][(X a) 0 (Z a)])
                dist (Vector3/Distance (->v3 (X a) 0 (Z a)) (->v3 (X b) 0 (Z b)))]

            (doseq [i (take (+ 2 (srand 12)) (range (int (/ (+ (Y lowest) 10.0) dist))))]
              (let [target (v- [(X a)(Y lowest)(Z a)] [0 (* i dist) 0])]
                (when (or (= i 0) (< 0 (noise :scaffold (->v3 (v* target 0.01)))))
                  (when-let [o (last @SCAFFOLDS)]
                    (position! o target)
                    (reset! SCAFFOLDS (cons o (butlast @SCAFFOLDS)))
                    (local-scale! o (->v3 (/ dist 10) (/ dist 10) (/ dist 10)))
                    (set! (.rotation (.transform o)) rot)
                    ))))
            b))
        points)))
 
(defn make-train [_]
  (dorun (for [z (range 4)] (position! (make-kart) [0 0 (* z 5)])))
  (let [hook (.AddComponent (clone! :rock_5) hooks.UpdateHook)]
    (set! (.namespaceName hook) "coaster.core")
    (set! (.varName hook) "update-test"))
  (let [hook (.AddComponent (clone! :rock_5) hooks.OnDrawGizmosHook)]
    (set! (.namespaceName hook) "coaster.core")
    (set! (.varName hook) "draw-gizmos")))

(defn update-test [_]
  (swap! T + Time/deltaTime)
    (let [underage (-  (+ buffer (int (* @T speed))) (count @track-positions))]
    (when (> underage 4) 
      (grow-track! underage)
      ))
  (vec (map-indexed
    (fn [i rider]
      (let [pos (spline (+ (/ @T speed) (* i interval)) @track-positions)
            next-pos (spline (+ (* @T speed) (* i interval) interval)  @track-positions)]
        (position! rider (V+ pos (->v3 [0 5 0])))
        (look-at! (->transform rider) (V+ next-pos (->v3 [0 5 0]))))) 
    (arcadia.core/objects-named "cart") ))
   (comment (mapv (comp #(force! %  (- (rand 100) 50) 0 0 ) ->rigidbody) 
      (arcadia.core/objects-named "hand.R"))) 
  (look-at! (->transform (the Camera)) (->v3 (the cart)))
  (draw-terrain (->v3 (the Camera))))

(defn draw-whole-track [go]
  (doseq [[a b] (partition 2 1 @track-positions)]
    (Gizmos/DrawLine a b)))

(defn draw-gizmos [_]
  (comment (apply on-draw-gizmos (mapv #(take 2 (drop (int (* @T speed)) %)) @track-positions @track-normals)))
  (set! Gizmos/color (color 1 0 1))
    (dorun (map-indexed #(Gizmos/DrawLine %2 (get @track-positions (inc %1) %2)) @track-positions)))


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
  (grow-track! buffer)
  (make-train nil)

  (set! (.name (clone! :Sphere (->v3 [0 0 0]))) "scaffold")
  (reset! SCAFFOLDS
    (vec (for [i (range 800)] 
      (let [o (clone! :girder)] 
        (parent! o (the scaffold)) o))))

  (let [seat (last (shuffle (arcadia.core/objects-named "empty-seat")))]
    (position! (the Camera)  (v+ (->v3 seat) [0 1.2 0]))
    (parent! (the Camera) seat)
    (look-at! (->transform (the Camera)) (->v3 (the cart)))
    (sel! (the Camera))))

(defn test-scene [go]
  (setup-game))


(setup-game)

(defn track-grower [go]
)
 
(grow-track! 2)