(ns hooks
  (:use arcadia.core)
  (:require [arcadia.messages])
  (:import [clojure.lang RT IFn Var]))

(defmacro emit-components []
  `(do ~@(map (fn [[message args]]
                (let [ns-field 'namespaceName
                      var-field 'varName
                      this (gensym "this")
                      arg-forms (map #(with-meta (gensym) {:tag %}) args)]
                  `(defcomponent
                     ~(symbol (str message "Hook"))
                     [~(with-meta ns-field {:tag 'String})
                      ~(with-meta var-field {:tag 'String})]
                     (~message
                       [~this ~@arg-forms]
                       (require (symbol ~ns-field))
                       (let [^Var v# (RT/var ~ns-field ~var-field)]
                         (if (bound? v#)
                           (.invoke v# (.gameObject ~this) ~@arg-forms))
                         )))))
              arcadia.messages/messages)))

(emit-components)