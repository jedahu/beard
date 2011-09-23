# Beard

Beard is a DSL for wiring [torus](https://github.com/jedahu/torus) handlers
and middleware. Inspired by [Moustache](https://github.com/cgrand/moustache).


## Synopsis

    (ns your.ns
      (:require
        [me.panzoo.torus :as torus]
        [me.panzoo.beard :as beard])
      (:require-macro
        [me.panzoo.beard.macro :as b]))

    (def routes
      (app
        middleware1
        (middleware2 arg)

        [] root-handler               ; requests to /
        ["foo"] foo-handler           ; requests to /foo
        ["bar" &] bar-handler         ; requests to /bar and /bar/.*
        ["user" :uname] user-handler  ; requests to /user/[^/]+
        [&] catchall-handler))        ; all unmatched requests

    (defn ^:export run []
      (torus/init routes))

Zero or more middleware may be provided. Each route vector may contain
strings, keywords, and the symbol `&`.

Beard adds the following keys to the torus request map:

    :path-args
      (IPersistentMap)
      A map of keyword keys to string values. For the above user-handler,
      given a URI path of /user/jack, the map would be {:uname "jack"}.

    :path-rest
      (ISeq)
      A sequence of left-over URI segments. For the above bar-handler, given
      a URI path of /bar/one/two/three, the sequence would be
      ["one" "two" "three"].
