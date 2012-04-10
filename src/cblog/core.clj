(ns cblog.core
  (:use [cblog routes db]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.jetty] [monger.core :as m]))

;(info "entering main")
; connect to db!
(let [x (zipmap [:user :password :host] (rest (first (re-seq  #"[\\/]{2}([^:]+):([^@]+)@([^/]+)" (System/getenv "MONGOHQ_URL")))))]
 (do (m/connect! {:host (:host x)}) (m/set-db! (m/get-db "cblog")) (m/authenticate m/*mongodb-database* (:host x) (:user x) (:password x)))) 

;(connect-to-db! {:host  :port})
(def routes (init-routes!))
(defn -main [] (let [port (Integer/parseInt (System/getenv "PORT"))] (run-jetty routes {:port port})))

