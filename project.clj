(defproject
  me.panzoo/beard "0.1.0-SNAPSHOT"

  :extra-namespace-dirs ["src"]

  :description "A micro DSL to wire torus handlers and middleware"

  :dependencies
  [[jasminejs "0.1.0-SNAPSHOT"]]
  
  :cljs
  {:optimizations :whitespace
   :pretty-print true
   :output-to "out/all.js"
   :output-dir "out"
   :test-cmd ["phantomjs" "test.js"]}
  
  :story
  {:output "doc/index.html"})
