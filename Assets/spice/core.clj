(ns spice.core
  (:use arcadia.core
        hard.core
        hard.animation)
  (:import [UnityEngine Debug Quaternion]))

(def tummies (atom []))

(defn build-tummy [person]
  (let [teeth (child-named person "top-teeth")
        puker (clone! :Puker)
        pt (.transform puker)]
    (parent! puker teeth)
    (set! (.localRotation pt) (Quaternion. 0.7071068 0.0 0.0 0.7071067))
    (set! (.localPosition pt) (->v3 [0 0 0]))
    {:volume (+ 500 (rand 200))
     :snack-propensity (rand 2)
     :terror-quotient (+ 1 (rand 1))
     :smeef (rand 1)
     :queasiness 0
     :is-vomming false
     :puker puker
     :animator (child-named person "infihead")}))

(defn start-quease! []
  (let [people (objects-named "stable")
        tums   (map build-tummy people)]
    (reset! tummies tums)))

(defn -pos [a b] (let [c (- a b)] (if (< c 0) 0 c)))

(defn quease
  [upsetfulness {:keys [volume snack-propensity
                        terror-quotient
                        smeef queasiness]}]
  (+ (* queasiness (/ (+ snack-propensity (- (rand 0.2) 0.1))
                      snack-propensity))
     (* terror-quotient upsetfulness (+ 1 (rand 1)))
     smeef))

(defn update-tummy [upsetfulness]
  (fn [tummy]
    (if (:is-vomming tummy)
      (let [new-queasiness (-pos (:queasiness tummy) 10)
            still-vomming (> new-queasiness 0)]
        (assoc tummy
               :queasiness new-queasiness
               :is-vomming still-vomming
               :swapped (not still-vomming)))
      (let [new-queasiness (quease upsetfulness tummy)
            is-vomming (> new-queasiness (:volume tummy))]
        (assoc tummy
               :queasiness new-queasiness
               :is-vomming is-vomming
               :swapped is-vomming)))))

(defn update-tummies [upsetfulness]
  (fn [tummies] (map (update-tummy upsetfulness) tummies)))

(def ticker (atom 0))

(defn update! [_]
  (let [upsetfulness (* (swap! ticker #(+ 0.01 %)) (rand 2))
        new-tummies (swap! tummies (update-tummies upsetfulness))]
    (doseq [vommer new-tummies]
      (let [vomming (:is-vomming vommer)]
        (set! (.active (:puker vommer)) vomming)
        (if (:swapped vommer)
          (play-state (:animator vommer)
                      (if vomming" sick1" "fear1")))))))
