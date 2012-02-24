(ns beard)

(defmacro app [& forms]
  (let [[midware routes] (split-with #(or (symbol? %) (list? %)) forms)]
    `(beard.core/app #(-> % ~@(reverse midware)) ~@routes)))
