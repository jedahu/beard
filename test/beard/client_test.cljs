(ns beard.client_test
  (:require
    [beard.core :as _])
  (:use-macros
    [beard :only (app)]
    [jasminejs.core :only (describe it expect)]))

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
    (conj {:history-state nil
           :location {:pathname uri}}
          (apply hash-map opts))))

(defn caught? [handler uri & opts]
  (= "catchall" (:id (apply request handler uri opts))))

(describe "app"
  (it "should create a handler"
    (expect truthy
      (app ["foo" "bar"] hello)))
  (it "should route a simple request"
    (expect =
      {:id "hello" :html "Hello World"}
      (request (app ["foo" "bar"] hello) "/foo/bar")))
  (it "should return nil for bad routes"
    (expect =
      nil
      (request (app ["foo" "bar"] hello) "/foo")))
  (it "should feed bad routes to the catchall handler"
    (expect truthy
      (caught? (app ["foo" "bar"] hello catchall) "/foo"))
    (expect truthy
      (caught? (app ["foo" "bar"] hello catchall) "/foo/bar/"))
    (expect truthy
      (caught? (app ["foo" "bar"] hello catchall) "/foo/bar/baz")))
  (it "should pass the rest of the path to the handler (for open routes)"
    (expect =
      []
      (:path-rest (request (app ["foo" &] detail) "/foo")))
    (expect =
      [""]
      (:path-rest (request (app ["foo" &] detail) "/foo/")))
    (expect =
      ["bar" "baz"]
      (:path-rest (request (app ["foo" &] detail) "/foo/bar/baz"))))
  (it "should supply path args to the handler"
    (expect =
      {:bar "one" :baz "two"}
      (:path-args (request (app ["foo" :bar :baz] detail) "/foo/one/two"))))
  (it "should nest inside another app"
    (expect =
     {:id "hello" :html "Hello World"}
      (request (app ["foo" &] (app ["bar"] hello)) "/foo/bar")))
  (it "should work with middleware"
    (expect =
      "<p>Hello World</p>"
      (:html (request (app wrap-html [] hello) "/")))))

;;. vim: set lispwords+=describe,it,expect,expect-not:
