(ns cblog.core
  (:refer-clojure :exclude [count])
  (:use [cblog routes db]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.jetty] [monger.core :as m]))

(def routes (init-routes!))

(try (let [mongo-url (System/getenv "MONGOHQ_URL")] (do (info (str (-> mongo-url "MONGOHQ_URL"))) (m/connect-via-uri! mongo-url))) 
  (catch Exception e (do (info "MONGOHQ_URL not set, using localhost") (connect-to-db!))))

(defn -main [] (let [port (Integer/parseInt (System/getenv "PORT"))] (run-jetty routes {:port port})))

