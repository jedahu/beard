(ns ring.middleware.beard)

(defn beard-handler
  [head & {:keys [html-mime]}]
  (let [resp {:status 200
              :headers ["Content-Type" (or html-mime "text/html")]
              :body (str "<!doctype html><html><head>"
                         head
                         "</head><body></body></html>")}]
    (constantly resp)))
