(ns cblog.security
  (:use (sandbar core stateful-session auth form-authentication validation)))

(def security-policy
       [#".*\.(css|js|png|jpg|gif|ico)$" :any
        #"/admin.*"                   :admin 
        #"/permission-denied.*"       :any
        #"/login.*"                   :any 
        #"/index.html"                :any
        #"/"                          :any
        #".*"                         #{:admin :user}])

(defrecord DemoAdapter [] 
    FormAuthAdapter
    (load-user [this username password]
         (let [login {:username username :password password}]
                (cond (= username "member") (merge login {:roles #{:member}})
                (= username "admin")        (merge login {:roles #{:admin}})
                :else login)))
    (validate-password
         [this]
         (fn [m]
                (if (= (:password m) (:username m))
                         m
                         (add-validation-error m "Username and password do not match!")))))

(defn form-authentication-adapter [] (DemoAdapter.))

;(defn query [type]
;    (ensure-any-role-if (= type :top-secret) #{:admin}
;                                              (= type :members-only) #{:member}
;                                              (str (name type) " data")))


