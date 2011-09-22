(ns me.panzoo.beard)

(defn uri-segments [uri]
  (let [segs (map js/decodeURIComponent (rest (.split uri #"/")))]
    (if (= "" (first segs))
      (rest segs)
      segs)))

(defn- uri [segments]
  (apply str "/" (interpose "/" (map js/encodeURIComponent segments))))

(defn alter-request [handler f & opts]
  #(handler (f % opts)))

(defn alter-response [handler f & opts]
  #(f (handler %) opts))

(defn- regex? [x]
  (instance? js/RegExp x))

(defn- match-route [segments route]
  (if (and (empty? (doall segments)) (empty? route))
    {}
    (loop [r {} segments segments route route]
      (if-let [x (first route)]
        (cond
          (string? x) (when (= x (first segments))
                        (recur r (rest segments) (rest route)))
          (regex? x) (when (re-matches x (first segments))
                       (recur r (rest segments) (rest route)))
          (or (= :& x) (= '& x)) (assoc r :path-rest segments)
          :else (when-let [segment (first segments)]
                  (recur (assoc r (first route) segment)
                         (rest segments) (rest route))))
        (when-not (seq segments) r)))))

(defn app [middleware & forms]
  (let [pairs (partition 2 forms)]
    ((or middleware identity)
      (fn [{:keys [path-rest] :as path-args}]
        (loop [[[route handler] & tail] pairs]
          (if-let [path-args1 (match-route path-rest route)]
            (handler {:path-args (merge path-args path-args1)})
            (when (seq tail) (recur tail))))))))

(comment defn app [middleware & forms]
  (let [pairs (partition 2 forms)]
    ((or middleware identity)
      (fn [{:keys [path-rest] :as req}]
        (loop [[[route handler] & tail] pairs]
          (if-let [req1 (match-route path-rest route)]
            (let [path-args (merge (:path-args req) (:path-args req1))]
              (handler (assoc (merge req req1) :path-args path-args))
            (do
              (when (seq tail) (recur tail))))))))))

(defn run-app [handler]
  (fn [req]
    (.log js/console "segs" (. js/location pathname))
    (handler (assoc req :path-rest (uri-segments (. js/location pathname))))))
