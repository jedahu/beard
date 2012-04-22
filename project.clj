(defproject
  beard "0.1.0"

  :description "A micro DSL to wire torus handlers and middleware"

  :dependencies
  [[menodora "0.1.2"]] 

  :plugins
  [[lein-cst "0.2.1"]]

  :story
  {:output "doc/index.html"}
  
  :cst
  {:suites [beard.test.client/core-tests]})
