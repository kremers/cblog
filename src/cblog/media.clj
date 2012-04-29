(ns cblog.media
  (:require [aws.sdk.s3 :as s3] [ring.util.mime-type :as mt])
  (:use  [cheshire.core] [cblog.db] [clojure.tools.logging :only [info debug error]])
)

(defn cred [] (let [settings (settings-overview)] {:access-key (:s3accesskey settings) :secret-key (:s3secretkey settings) }))
(defn bucket [] (let [settings (settings-overview)] (:s3bucketname settings)))
(defn media_list [] (let [objects (s3/list-objects (cred) (bucket))] (do (info (with-out-str (clojure.pprint/pprint objects))) objects )))
(defn media_upload [k v & [metad]] (do (s3/put-object (cred) (bucket) k v metad) (s3/update-object-acl (cred) (bucket) k (s3/grant :all-users :read))))
(defn media_remove [k] (s3/delete-bucket (cred) k))
(defn media_get [k] (slurp (:content (s3/get-object (cred) (bucket) k))))
(defn media-exists? [k] (s3/object-exists? (cred) (bucket) k))

(defn medialist_json [] (generate-string (:objects (media_list))))

(defn handle-submit [req] (let [filename ((:query-params req) "qqfile") input (:body req) clength (:content-length req)] 
                           (do 
                             (debug (with-out-str (clojure.pprint/pprint req))) 
                             ;(info (str "clength? " clength " = " (count (to-byte-array input))))
                            (media_upload filename input {:content-length clength :content-type (mt/ext-mime-type filename)} )
                            ;(info (str "size of headers: " (count (:headers req))))
                             )))

;(with-out-str (clojure.pprint/pprint
