(ns hard.animation
  (:use 
  arcadia.core 
  arcadia.hydrate
  hard.core
  hard.animation
  hard.edit)
  (:import [UnityEngine]))


(defn rig [o] 
  (reveal-bones o)
  (form-colliders o))


(do 
  (clear-cloned!)
  (let [
        base (clone! :humans/human-armature)
        torso (child-named base "spine")]
  
    (rig torso) )

  )

(let [
        base (clone! :humans/human-armature-low)
        torso (child-named base "spine")]
  
    (rig torso) )


(defn ->skinned [o]
  (->comp (->go o) UnityEngine.SkinnedMeshRenderer))

(defn shape-count [o]
  (.blendShapeCount (.sharedMesh (->skinned o))))




(do 
  (clear-cloned!)
  (dorun 
    (for [z (range 2)
          y (range 3)
          :let [head (clone! :humans/infihead [(* y -0.5) y z])]]
  
  (let [sm (->skinned head)]
    (local-scale! head (rand-vec [0.8 1.2] [0.9 1.2] [1 1]))
    (set! (.color (.material (.GetComponent head UnityEngine.Renderer))) (color (rand-vec 1 1 1)))
    (dorun (for [i (range 17)]
      (if true
      (.SetBlendShapeWeight sm (int i) (Mathf/Pow (rand 10) 2)))))
    (log (mapv #(.GetBlendShapeWeight sm (int %)) (range 17)))))))


