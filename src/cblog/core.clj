(ns cblog.core
  (:refer-clojure :exclude [count])
  (:use [cblog routes db]
        [clojure.tools.logging :only (info error)]
        [ring.adapter.jetty] [monger.core :as m]))

(def routes (init-routes!))

(try (let [mongo-url (System/getenv "MONGOHQ_URL")] (do (m/connect-via-uri! mongo-url) (info (str "MONGOHQ_URL: " mongo-url)))) 
  (catch NullPointerException e (do (info "MONGOHQ_URL not set, using localhost") (connect-to-db!))))

(defn -main [] (try (let [port (Integer/parseInt (System/getenv "PORT"))] (do (run-jetty routes {:port port}) (info (str "PORT: " port))))
  (catch Exception e (do (info "PORT not set, using 8080") (run-jetty routes {:port 8080})))))

;(defn -main [] (let [port (Integer/parseInt (System/getenv "PORT"))] (run-jetty routes {:port port})))

