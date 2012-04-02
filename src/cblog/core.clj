(ns cblog.core
  (:use [cblog routes db]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.netty]))

(info "entering main")
(connect-to-db!)
(def routes (init-routes!))

