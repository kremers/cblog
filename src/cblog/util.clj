(ns cblog.util
  (:import [java.io BufferedReader] [java.io InputStreamReader]))

; Macro to recieve a BufferedReader from a request body
(defmacro with-req-body [[binding] req & body] `(with-open [r# (~req :body)] (let [~binding (BufferedReader. (InputStreamReader. r#))] ~@body)))

; To ensure utf8 reply
(defn utf8response "Ring skeleton with headers." [body] {:status  200 :headers { "Content-Type" "text/html;charset=UTF-8" "Character-Encoding" "UTF-8"} :body body})
