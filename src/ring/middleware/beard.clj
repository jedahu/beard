(ns ring.middleware.beard)

(defn beard-handler
  [& {:keys [head body html-mime]}]
  (let [resp {:status 200
              :headers ["Content-Type" (or html-mime "text/html")]
              :body (str "<!doctype html><html><head>"
                         head
                         "</head><body>"
                         (or body
                             "<p>Javascript is required for this site.</p>") 
                         "</body></html>")}]
    (constantly resp)))
