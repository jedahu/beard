(ns beard.core)

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

(defn match-route [segments route]
  (if (and (empty? (doall segments)) (empty? route))
    {}
    (loop [r {} segments segments route route]
      (if-let [x (first route)]
        (cond
          (string? x) (when (= x (first segments))
                        (recur r (rest segments) (rest route)))
          (regex? x) (when (re-matches x (first segments))
                       (recur r (rest segments) (rest route)))
          (= '& x) {:path-args r :path-rest segments}
          :else (when-let [segment (first segments)]
                  (recur (assoc r (first route) segment)
                         (rest segments) (rest route))))
        (when-not (seq segments) {:path-args r})))))

(defn app [middleware & forms]
  (let [pairs (partition 2 forms)]
    ((or middleware identity)
      (fn [{:keys [path-args path-rest] :as req}]
        (let [path-rest (if (or path-args path-rest)
                          path-rest
                          (uri-segments (:pathname (:location req))))]
          (loop [[[route handler] & tail] pairs]
            (if-let [path-args+rest (match-route path-rest route)]
              (handler (assoc
                         req
                         :path-args (merge
                                      path-args
                                      (:path-args path-args+rest))
                         :path-rest (:path-rest path-args+rest)))
              (if (seq tail)
                (recur tail)
                nil))))))))
