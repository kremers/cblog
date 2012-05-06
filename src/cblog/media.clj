(ns cblog.media
  (:import [org.imgscalr.Scalr.Method] [java.awt.image BufferedImage BufferedImageOp]
           [javax.imageio.ImageIO] [java.io PrintWriter PipedInputStream PipedOutputStream ByteArrayOutputStream ByteArrayInputStream]
           [org.imgscalr.Scalr] [org.imgscalr.Scalr.Method] )
  (:require [aws.sdk.s3 :as s3] [ring.util.mime-type :as mt] )
  (:use  [cheshire.core] [cblog.db] [cblog.util] [clojure.tools.logging :only [info debug error]]
        [clojure.java.io :only (as-url)]))

(defn cred [] (let [settings (settings-overview)] {:access-key (:s3accesskey settings) :secret-key (:s3secretkey settings) }))
(defn bucket [] (let [settings (settings-overview)] (:s3bucketname settings)))
(defn media_list [] (let [objects (s3/list-objects (cred) (bucket))] (do (debug (with-out-str (clojure.pprint/pprint objects))) objects )))
(defn media_upload [k v & [metad]] (do (s3/put-object (cred) (bucket) k v metad) (s3/update-object-acl (cred) (bucket) k (s3/grant :all-users :read))))
(defn media_remove [k] (s3/delete-object (cred) (bucket) k))
(defn media_get [k] (slurp (:content (s3/get-object (cred) (bucket) k))))
(defn media-exists? [k] (s3/object-exists? (cred) (bucket) k))
(defn medialist_json [] (generate-string (:objects (media_list))))

;(defn gen-thumbnail "returns input stream to image" [url x y] 
;  (let [in-stream (PipedInputStream.)]  
;  (future (with-open [out (-> in-stream (PipedOutputStream.))]
;    (-> (net.coobird.thumbnailator.Thumbnails/fromURLs [(as-url url)]) (.size x y) (.outputFormat "jpg") (.toOutputStream out)))) in-stream))

(defn gen-thumbnail "returns input stream to image" [url x y] 
  (let [#^java.awt.image.BufferedImage old (javax.imageio.ImageIO/read (as-url url)) 
        out (org.imgscalr.Scalr/resize old x y (into-array BufferedImageOp []))]
    (with-open [aout (ByteArrayOutputStream.)] 
      (do (.flush old) (javax.imageio.ImageIO/write out "jpeg" aout) (ByteArrayInputStream. (.toByteArray aout))))))

;(defn gen-thumbnail "returns input stream to image" [url x y]
;  (let [aout (ByteArrayOutputStream.)]
;  (javax.imageio.ImageIO/write 
;    (-> (net.coobird.thumbnailator.Thumbnails/fromURLs [(as-url url)]) (.size x y) (.asBufferedImage)) "jpeg" aout)
;   (new ByteArrayInputStream (.toByteArray aout) )))

(defn tgen [src x y] (let [fspl (re-find #"([^/]+)\.([^\.]+)$" src) fname (fspl 1) fext (fspl 2) tnail (gen-thumbnail src x y)] 
  (media_upload (str "thumbnails/" fname "_" x "_" y "." fext) (gen-thumbnail src x y) {:content-type "image/jpeg"})))

(defn handle-submit [req] (let [filename (clojure.string/lower-case ((:query-params req) "qqfile")) 
                                input (:body req) 
                                clength (:content-length req) 
                                ctype (mt/ext-mime-type filename) ] 
                           (do (debug (with-out-str (clojure.pprint/pprint req))) 
                            (media_upload filename input {:content-length clength :content-type ctype})
                            (when (re-matches #"^image.+" ctype) (tgen (str "http://s3.amazonaws.com/" (bucket) "/" filename) 150 150)))))

(defn media_redirect [key] {:status 301 :headers {"Location" (str "http://s3.amazonaws.com/" (bucket) "/" key)} :body ""})
(defn delete-media [req] (media_remove (:key (json-in req))))




