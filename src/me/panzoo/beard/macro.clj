(ns me.panzoo.beard.macro)

(defmacro app [& forms]
  (let [[midware routes] (split-with #(or (symbol? %) (list? %)) forms)]
    `(me.panzoo.beard/app #(-> % ~@(reverse midware)) ~@routes)))
