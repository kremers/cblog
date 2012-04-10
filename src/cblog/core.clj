(ns cblog.core
  (:refer-clojure :exclude [count])
  (:use [cblog routes db]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.jetty] [monger.core :as m]))

;(info "entering main")
; connect to db!
;(let [x (zipmap [:user :password :host :db] (rest (first (re-seq  #"[\\/]{2}([^:]+):([^@]+)@([^/]+)/(.+)" (System/getenv "MONGOHQ_URL")))))]
; (do (info (System/getenv "MONGOHQ_URL")) (info (str "db: " (:db x))) (m/connect! {:host (:host x)}) (m/set-db! (m/get-db (:db x))) (m/authenticate (:host x) (:user x) (:password x)))) 
(info  (System/getenv "MONGOHQ_URL"))
(m/connect-via-uri! (System/getenv "MONGOHQ_URL"))

;(connect-to-db! {:host  :port})
(def routes (init-routes!))
(defn -main [] (let [port (Integer/parseInt (System/getenv "PORT"))] (run-jetty routes {:port port})))

