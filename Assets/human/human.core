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