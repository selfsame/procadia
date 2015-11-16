(ns hard.animation
 (:use arcadia.core hard.core
  hard.physics)
  (:import [UnityEngine]))

 
 ;mechanim animator
(defn ->animator [o]
  (.GetComponentInChildren o UnityEngine.Animator))

(defn state-info [o]
	(.GetCurrentAnimatorStateInfo (->animator o) 0))

(defn is-name [o s]
	(.IsName (state-info o) s))

(defn play-state [o s] (.Play (->animator o) s))

(defn cross-fade [o s t] (.CrossFade (->animator o) s t))

(defn param-bool 
  ([o s] (.GetBool (->animator o) s))
  ([o s v] (.SetBool (->animator o) s v)))

(defn param-float 
  ([o s] (.GetFloat (->animator o) s))
  ([o s v] (.SetFloat (->animator o) s v)))


;legacy animations

(defn ->animation [o]
  (.GetComponentInChildren o UnityEngine.Animation))
 
(arcadia.core/defcomponent Bone [^float length]
	(Start [this] 
		(if-let [nex (first (rest (children (->go this))))]
		  (! this length (.magnitude (V- (->v3 nex) (->v3 this))))))
	(OnDrawGizmos [this] 
		(gizmo-color (color 1 0 1))
		(gizmo-point (->v3 this) 0.01)
		(gizmo-line  (->v3 this) (transform-point 
      (->transform (->go this)) (->v3 (- (.length this)) 0 0) )))
	(OnDrawGizmosSelected [this] 
		(gizmo-color (color 0 1 1))
		(gizmo-point (->v3 this) 0.012)
		(gizmo-line (->v3 this) (transform-point 
      (->transform (->go this)) (->v3 (- (.length this)) 0 0) ))))

(defn ->bone [o] (->comp (->go o) "hard.animation.Bone"))

(comment 

(defn reveal-bones [o]
	(let [nexts (children o)]
		(let [gob (->go o)
			  bone (do (destroy! (->bone gob )) 
                 (.AddComponent gob hard.animation.Bone))
			  lens (map #(* 1 (.magnitude (V- (->v3 %) (->v3 o)))) nexts)
			  res (cond (empty? lens) 0.05
			  	(= 1 (count lens)) (first lens)
			  	:else (/ (apply + lens) (count lens)))]
			  	(do
			  		(set! (.length bone) (float res))
					(mapv reveal-bones nexts)))))

(defn form-colliders [o]
  (let [nex (->bone o)
        prv (parent o)
        offset (float (if (->bone prv) (.length (->bone prv)) 0))]

      (populate! (->go nex) {
        :capsule-collider [{
          :direction (int 0)
          :height offset
          :center  (->v3 (* offset -0.5) 0.0 0.0)
          :radius (float 0.22) }]
        :rigidbody [{:use-gravity false}]
        :hinge-joint [{
          :axis (->v3 0 1 0)
          :auto-configure-connected-anchor false
          :connected-anchor (->v3 (- offset) 0 0)
          :connected-body (->rigidbody (->go prv))
          }]
        })
      
    (mapv form-colliders 
      (filter ->bone (children nex))))))




