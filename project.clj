(defproject cblog "0.1.0-SNAPSHOT"
  :description "simple blog in clojure"
  :dev-dependencies [[lein-ring "0.5.2"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring "1.0.0"]
                 [clj-time "0.3.3"]
                 [me.shenfeng/async-ring-adapter "1.0.0"]
                 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [cheshire "2.0.4"]
                 [ch.qos.logback/logback-classic "1.0.0"]
                 [stencil "0.2.0"]
                 [com.novemberain/monger "1.0.0-beta2"]
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]]
   :main cblog.core
   :source-path "src"
   :java-source-path "jsrc"
   :ring {:handler cblog.core/routes}
   :properties { :project.build.sourceEncoding "UTF-8" }
)
