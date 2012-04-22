(ns beard)

(defmacro app [& forms]
  (let [[midware routes] (split-with #(or (symbol? %) (list? %)) forms)]
    `(beard.core/app
       #(-> % ~@(reverse midware))
       ~@(for [i (range (count routes))
               :let [x (nth routes i)]]
           (if (even? i)
             (if (= '& (last x))
               (conj (pop x) ''&)
               x)
             x)))))
