(ns cblog.core
  (:use [cblog.routes]
        [monger.core :only [connect! connect set-db! get-db]]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.netty]
  ))

(info "entering main")

; connect to db
(connect!)

; initialize routes
(def routes (init-routes!))

