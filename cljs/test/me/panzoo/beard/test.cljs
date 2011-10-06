(ns me.panzoo.beard.test
  (:use
    [me.panzoo.beard :only (app)]))

(defn hello [_]
  {:id "hello"
   :content (delay "Hello World")})

(defn detail [req]
  {:id "detail"
   :path-args (:path-args req)
   :path-rest (:path-rest req)})

(defn catchall [_]
  {:id "catchall"
   :content (delay "Error")})

(defn wrap-content [handler & [s]]
  (fn [req]
    (let [res (handler req)]
      (assoc res :content (delay (str @(:content res) (or s "-default")))))))

(defn request [handler uri & opts]
  (handler
    (conj {:history-state nil
           :location {:pathname uri}}
          (apply hash-map opts))))

(defn found+content [handler uri & opts]
  (when-let [c (:content (apply request handler uri opts))]
    @c))

(defn found+detail [handler uri & opts]
  (apply request handler uri opts))

(defn not+found [handler uri & opts]
  (= "catchall" (:id (apply request handler uri opts))))

(assert (app nil ["foo" "bar"] hello)
        "Handler creation failed.")
(assert (request (app nil ["foo" "bar"] hello) "/foo/bar")
        "Request failed.")
(assert (found+content (app nil ["foo" "bar"] hello) "/foo/bar")
        "Fixed route failed.")
(assert (try
          (found+content (app nil ["foo" "bar"] hello) "/foo")
          false
          (catch js/Error _
            true))
        "Dispatch error not thrown.")
(assert (not+found (app nil ["foo" "bar"] hello catchall) "/foo")
        "Catchall failed.")
(assert (not+found (app nil ["foo" "bar"] hello catchall) "/foo/bar/")
        "Catchall failed.")
(assert (not+found (app nil ["foo" "bar"] hello catchall) "/foo/bar/baz")
        "Catchall failed.")
(assert (= [] (:path-rest (found+detail (app nil '["foo" &] detail)
                                         "/foo")))
        "Open route failed.")
(assert (= [""] (:path-rest (found+detail (app nil '["foo" &] detail)
                                           "/foo/")))
        "Open route failed.")
(assert (= ["bar" "baz"] (:path-rest (found+detail
                                       (app nil '["foo" &] detail)
                                       "/foo/bar/baz")))
        "Open route failed.")
(assert (= {:bar "one"
            :baz "two"}
           (:path-args (found+detail
                         (app nil ["foo" :bar :baz] detail)
                         "/foo/one/two")))
        "Path args failed.")
(assert (found+content (app nil '["foo" &] (app nil ["bar"] hello)) "/foo/bar")
        "Nested app failed.")
(assert (= "Hello World-default"
           @(:content (found+detail (app wrap-content [] hello) "/")))
        "Middleware failed.")
