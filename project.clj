(defproject cblog "0.1.0-SNAPSHOT"
  :description "simple blog in clojure"
  :dev-dependencies [[lein-ring "0.6.3"]]
  :dependencies [[ring/ring-jetty-adapter "1.1.0-beta3"]
                 [org.clojure/clojure "1.3.0"]
                 [ring "1.0.0"]
                 [clj-time "0.3.3"]
                 [me.shenfeng/async-ring-adapter "1.0.0"]
                 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [cheshire "2.0.4"]
                 [ch.qos.logback/logback-classic "1.0.0"]
                 [stencil "0.2.0"]
                 [com.novemberain/monger "1.0.0-SNAPSHOT"]
                 [kremers/sandbar "0.4.1-SNAPSHOT"]
                 [kremers/monger-session "0.9.0"]
                 [clj-aws-s3 "0.3.0"]
                 ]
   :main cblog.core
   :source-path "src"
   :ring {:handler cblog.core/routes}
   :properties { :project.build.sourceEncoding "UTF-8" }
)
