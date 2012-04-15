(ns cblog.core
  (:use
    [cblog.db :only [connect-to-db!]]
    [cblog.routes :only [init-routes!]]
    [clojure.tools.logging :only [info]]
    [monger.core :only [connect-via-uri!]]
    [ring.adapter.jetty :only [run-jetty]]))

(def routes (init-routes!))

(try (let [mongo-url (System/getenv "MONGOHQ_URL")] (do (connect-via-uri! mongo-url) (info (str "MONGOHQ_URL: " mongo-url)))) 
  (catch NullPointerException e (do (info "MONGOHQ_URL not set, using localhost") (connect-to-db!))))

(defn -main [] (try (let [port (Integer/parseInt (System/getenv "PORT"))] (do (run-jetty routes {:port port}) (info (str "PORT: " port))))
  (catch Exception e (do (info "PORT not set, using 8080") (run-jetty routes {:port 8080})))))

