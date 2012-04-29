(ns cblog.media
  (:require [aws.sdk.s3 :as s3])
  (:use [cblog.db] [clojure.tools.logging :only [info debug error]])
)

(defn cred [] (let [settings (settings-overview)] {:access-key (:s3accesskey settings) :secret-key (:s3secretkey settings) }))
(defn bucket [] (let [settings (settings-overview)] (:s3bucketname settings)))
(defn media_list [] (s3/list-objects (cred) (bucket)))
(defn media_upload [k v] (do (s3/put-object (cred) (bucket) k v) (s3/update-object-acl (cred) (bucket) k (s3/grant :all-users :read))))
(defn media_remove [k] (s3/delete-bucket (cred) k))
(defn media_get [k] (slurp (:content (s3/get-object (cred) (bucket) k))))
(defn media-exists? [k] (s3/object-exists? (cred) (bucket) k))

;(defn pipe_to_s3 [fname in]
;    (let [out (PipedOutputStream. in) ] (.start (Thread. #(media_upload fname out))) in))

(defn handle-submit [req] (let [filename ((:query-params req) "qqfile") input (:body req) clength (:content-length req)] 
                           (do 
                             (debug (with-out-str (clojure.pprint/pprint req))) 
                             ;(info (str "clength? " clength " = " (count (to-byte-array input))))
                            (media_upload filename input)
                            ;(info (str "size of headers: " (count (:headers req))))
                             )))

;(with-out-str (clojure.pprint/pprint
