(ns spice.core
  (:use arcadia.core
        hard.core)
  (:import [UnityEngine Debug]))

(def tummies (atom []))

(defn build-tummy [menz]
  {:volume (+ 500 (rand 200))
   :snack-propensity (rand 2)
   :terror-quotient (+ 1 (rand 1))
   :smeef (rand 1)
   :queasiness 0
   :is-vomming false
   :puker (child-named menz "Puketicle System")})

(defn start-quease! []
  (let [people (objects-named "veryman")
        tums   (map build-tummy people)]
    (reset! tummies tums)))

(defn -pos [a b] (let [c (- a b)] (if (< c 0) 0 c)))

(defn quease
  [upsetfulness {:keys [volume snack-propensity
                        terror-quotient
                        smeef queasiness]}]
  (+ (* queasiness (/ (+ snack-propensity (- (rand 0.2) 0.1))
                      snack-propensity))
     (* terror-quotient upsetfulness (rand 1))
     smeef))

(defn update-tummy [upsetfulness]
  (fn [tummy]
    (if (:is-vomming tummy)
      (let [new-queasiness (-pos (:queasiness tummy) 10)
            still-vomming (> new-queasiness 0)]
        (assoc tummy
               :queasiness new-queasiness
               :is-vomming still-vomming))
      (let [new-queasiness (quease upsetfulness tummy)]
        (assoc tummy
               :queasiness new-queasiness
               :is-vomming (> new-queasiness (:volume tummy)))))))

(defn update-tummies [upsetfulness]
  (fn [tummies] (map (update-tummy upsetfulness) tummies)))

(defn update! [_]
  (let [upsetfulness (+ 0.5 (rand 0.5))
        new-tummies (swap! tummies (update-tummies upsetfulness))]
    (doseq [vommer new-tummies]
      (set! (.active (:puker vommer)) (:is-vomming vommer))
        ))) 
