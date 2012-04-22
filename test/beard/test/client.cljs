(ns beard.test.client
  (:require
    [beard.core :as bc]
    [menodora.core :as mc])
  (:use
    [menodora.predicates :only (eq truthy)]
    [beard.core :only (match-route)])
  (:use-macros
    [beard :only (app)]
    [menodora :only (defsuite describe should expect)]))

(defn hello [_]
  {:id "hello"
   :html "Hello World"})

(defn detail [req]
  {:id "detail"
   :path-args (:path-args req)
   :path-rest (vec (:path-rest req))})

(defn catchall [_]
  {:id "catchall"})

(defn wrap-html [handler & [s]]
  (fn [req]
    (let [res (handler req)]
      (assoc res :html (str "<p>" (:html res) "</p>")))))

(defn request [handler uri & opts]
  (handler
    (merge {:history-state nil
            :location {:pathname uri}}
           (apply hash-map opts))))

(defn caught? [handler uri & opts]
  (= "catchall" (:id (apply request handler uri opts))))

(defsuite core-tests
  (describe "match-route"
    (should "handle empty arguments"
      (expect eq
        {}
        (match-route [] [])))
    (should "match a simple route"
      (expect eq
        {:path-args {}}
        (match-route ["foo" "bar"] ["foo" "bar"])))
    (should "return extra path segments"
      (expect eq
        {:path-args {}
         :path-rest ["three" "four"]}
        (match-route ["one" "two" "three" "four"]
                     ["one" "two" '&]))))

  (describe "app"
    (should "create a handler"
      (expect truthy 
        (app ["foo" "bar"] hello)))
    (should "route a simple request"
      (expect eq
        {:id "hello" :html "Hello World"}
        (request (app ["foo" "bar"] hello) "/foo/bar")))
    (should "return nil for bad routes"
      (expect eq
        nil
        (request (app ["foo" "bar"] hello) "/foo")))
    (should "feed bad routes to the catchall handler"
      (expect truthy
        (caught? (app ["foo" "bar"] hello [&] catchall) "/foo"))
      (expect truthy
        (caught? (app ["foo" "bar"] hello [&] catchall) "/foo/bar/"))
      (expect truthy
        (caught? (app ["foo" "bar"] hello [&] catchall) "/foo/bar/baz")))
    (should "pass the rest of the path to the handler (for open routes)"
      (expect eq
        []
        (:path-rest (request (app ["foo" &] detail) "/foo")))
      (expect eq
        [""]
        (:path-rest (request (app ["foo" &] detail) "/foo/")))
      (expect eq
        ["bar" "baz"]
        (:path-rest (request (app ["foo" &] detail) "/foo/bar/baz"))))
    (should "supply path args to the handler"
      (expect eq
        {:bar "one" :baz "two"}
        (:path-args (request (app ["foo" :bar :baz] detail) "/foo/one/two"))))
    (should "nest inside another app"
      (expect eq
        {:id "hello" :html "Hello World"}
        (request (app ["foo" &] (app ["bar"] hello)) "/foo/bar")))
    (should "work with middleware"
      (expect eq
        "<p>Hello World</p>"
        (:html (request (app wrap-html [] hello) "/")))))) 

;;. vim: set lispwords+=defsuite,describe,should,expect:
