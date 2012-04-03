(ns cblog.util
  (:refer-clojure :exclude [extend replace reverse])
  (:import [java.io BufferedReader] [java.io InputStreamReader])
  (:use [sandbar.auth] [stencil.core] [hiccup core page-helpers]
        [clj-time.core] [clj-time.format] [clojure.string]))

; Macro to recieve a BufferedReader from a request body
(defmacro with-req-body [[binding] req & body] `(with-open [r# (~req :body)] (let [~binding (BufferedReader. (InputStreamReader. r#))] ~@body)))

; To ensure utf8 reply
(defn utf8response "Ring skeleton with headers." [body] {:status  200 :headers { "Content-Type" "text/html;charset=UTF-8" "Character-Encoding" "UTF-8"} :body body})

; "default" layout
(defn layout [content] (render-file "templates/default" { :capsule (html content) :username (current-username) }))

; 404 Message
(defn make-404 [] (str "404\n---\nCeci n'est pas une 404"))

; Generate timestamp
(defn gen-timestamp [] (unparse (formatters :date-hour-minute-second) (now)))


; The next "big" section is middleware to ignore the trailing /
(defn with-uri-rewrite
    "Rewrites a request uri with the result of calling f with the
       request's original uri.  If f returns nil the handler is not called."
    [handler f]
    (fn [request]
          (let [uri (:uri request)
                          rewrite (f uri)]
                  (if rewrite
                            (handler (assoc request :uri rewrite))
                            nil))))

(defn- uri-snip-slash
    "Removes a trailing slash from all uris except \"/\"."
    [uri]
    (if (and (not (= "/" uri))
                        (.endsWith uri "/"))
          (trim-newline uri)
          uri))

(defn ignore-trailing-slash
    "Makes routes match regardless of whether or not a uri ends in a slash."
    [handler]
    (with-uri-rewrite handler uri-snip-slash))
