(ns human.core
  (:use 
  arcadia.core 
  arcadia.hydrate
  hard.core
  hard.animation
  hard.edit
  hard.mesh
  math.spline
  seed.core
  human.data)
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
        ;head-ob (clone! :humans/infihead-medium)
        head (child-named rider "infihead")
        sm (->skinned head)
        skin-color (color (rand-vec 1 1 1))]
    ;(local-scale! head (rand-vec [0.8 1.2] [0.9 1.2] [1 1]))
    (mapv #(set! (.color (.material (.GetComponent % UnityEngine.Renderer))) skin-color)
      [head (child-named rider "body-mesh")
      (child-named rider "arm-mesh")])
   ;(parent! head-ob (child-named rider "head"))
    ;(position! head-ob [0 0 0])
    (dorun (for [i (range (shape-count head))]
      (if true
      (.SetBlendShapeWeight sm (int i) (Mathf/Pow (rand 10) 2)))))
    rider))





(defn make-dna 
  ([] (make-dna (rand-int 100000)))
  ([seed]
    (seed! seed)
    {:skin-color (srand-nth (vec (:skin-tones @DB)))
      :hair-color (srand-nth (vec (:hair-colors @DB)))
      :hair (srand-nth [:humans/hair-plain :humans/hair-bangs])
      }))

(comment 
  (import '[System IO.Path IO.File IO.StringWriter Environment])
  (binding [*print-length* nil](File/WriteAllText  "Assets/human/data.txt" (with-out-str (clojure.pprint/pprint  @DB))))

  (do 
  (clear-cloned!)
  (dorun (for [x (range 10) z (range 10) 
        :let [o (clone! :Sphere [x 0 z])
              [r g b] (rand-vec [0.0 1.0] [0.0 1.0] [0.0 1.0])
              c [r g b]]]
    (do 
      (material-color! o (color c))
      (set! (.name o) (apply str (interpose ", " (mapv #(re-find #"..." (str %)) c))))
       ))))

  (swap! DB update-in [:hair-colors] into (map material-color (sel))))

(defn rand-blend [mesh]
  (dorun 
    (for [i (range (shape-count mesh))]
      (when (< (srand) 0.8)
      (.SetBlendShapeWeight (->skinned mesh) (int i) (srand 100))))))


(def emotions ['joy1 'joy2 'joy3 'fear1 'fear2 'sick1 'sick2 'sick3 'anger1])

(do 
  (clear-cloned!)
  (dorun 
    (for [x (range 10) z (range 7)] 
      (let [target (v* [x 0 z] 1)
            dna (make-dna)
            ragdoll (clone! :humans/stable )
            o (clone! :humans/infihead )
            head-mesh (child-named o "infihead-mesh")
            arm-mesh (child-named ragdoll "arm-mesh")
            body-mesh (child-named ragdoll "body-mesh")
            hair (clone! (:hair dna))

            ragdoll-neck (child-named ragdoll "neck")]
        (material-color! head-mesh (:skin-color dna))
        (material-color! body-mesh (:skin-color dna))
        (material-color! arm-mesh (:skin-color dna))
        (material-color! hair (:hair-color dna))
        (parent! hair o)

        (rotate! o [90 0 0])
        (local-scale! o (->v3 0.5))
        
        (parent! o (child-named ragdoll "head"))
        (position! o (->v3 (child-named ragdoll "head")))
        (rotation! o (rotation  (child-named ragdoll "head")))

        (parent! (child-named o "neck") ragdoll-neck)

        (position! ragdoll (if (odd? z) (v+ target [0.5 0 0]) target))
        (sel! o)
        (rand-blend head-mesh)
        (rand-blend hair)
        (cross-fade o (str (srand-nth emotions)) 1.0)
        )))


    true)

    (mapv (comp #(force! % 0 -6000 -10000 ) ->rigidbody) 
      (arcadia.core/objects-named "head"))


    (mapv (comp #(force! % 0 1000 1000) ->rigidbody) 
      (arcadia.core/objects-named "arm.R"))