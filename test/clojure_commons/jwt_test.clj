(ns clojure-commons.jwt-test
  (:use [clojure.test]
        [slingshot.slingshot :only [try+]])
  (:require [buddy.core.keys :as keys]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure-commons.jwt :as jwt])
  (:import [clojure.lang ExceptionInfo]))

(def user {:user        "ipctest"
           :email       "ipctest@iplantcollaborative.org"
           :given-name  "Ipc"
           :family-name "Test"
           :common-name "Ipc Test"})

(def opts {:validity-window-end  300
           :public-key-path      "test-resources/public-key.pem"
           :private-key-path     "test-resources/private-key.pem"
           :private-key-password "testkey"
           :accepted-keys-dir    "test-resources/accepted-keys"
           :alg                  :rs256})

(def second-opts {:validity-window-end  300
                  :private-key-path     "test-resources/private-keys/second-key.pem"
                  :private-key-password "second"
                  :alg                  :rs256})

(def untrusted-opts {:validity-window-end  300
                     :private-key-path     "test-resources/private-keys/untrusted.pem"
                     :private-key-password "untrusted"
                     :alg                  :rs256})

(def accepted-jwks
  (mapv (comp #(assoc % :alg :rs256) keys/public-key->jwk keys/public-key)
        ["test-resources/public-key.pem"
         "test-resources/accepted-keys/second-key.pem"]))

(def expired-opts (assoc opts :validity-window-end -1))

(def generator (jwt/generator opts))

(def second-generator (jwt/generator second-opts))

(def untrusted-generator (jwt/generator untrusted-opts))

(def expired-generator (jwt/generator expired-opts))

(def validator (jwt/validator opts))

(defn exception-cause
  [f]
  (try+
   (f)
   (catch [:type :validation] o
     (:cause o))))

(deftest jwt-test
  (is (= user (jwt/user-from-default-assertion (validator (generator user))))
      "Can validate a generated assertion.")
  (is (= user (jwt/user-from-default-assertion (validator (second-generator user))))
      "Can validate an assertion generated by a third party.")
  (is (= :signature (exception-cause #(validator (untrusted-generator user))))
      "Untrusted signature message for untrusted signing key.")
  (is (= :exp (exception-cause #(validator (expired-generator user))))
      "Expired token message for expired token."))

(deftest jwk-test
  (is (= user (jwt/user-from-default-assertion (jwt/jwk-validate accepted-jwks (generator user))))
      "Can validate an assertion - first JWK.")
  (is (= user (jwt/user-from-default-assertion (jwt/jwk-validate accepted-jwks (second-generator user))))
      "Can validate an assertion - second JWK.")
  (is (= :signature (exception-cause #(jwt/jwk-validate accepted-jwks (untrusted-generator user))))
      "Untrusted signature message for untrusted signing key.")
  (is (= :exp (exception-cause #(jwt/jwk-validate accepted-jwks (expired-generator user))))
      "Expired token message for expired token."))

(defn- build-custom-assertion
  [validity-window-end {:keys [user email given-name family-name common-name]}]
  (let [now (time/now)]
    {:exp                                 (time/plus now (time/seconds validity-window-end))
     :iat                                 now
     :http://wso2.org/claims/enduser      (str "foo/" user)
     :http://wso2.org/claims/emailaddress email
     :http://wso2.org/claims/givenname    given-name
     :http://wso2.org/claims/lastname     family-name
     :http://wso2.org/claims/fullname     common-name}))

(defn- user-from-custom-assertion
  [jwt]
  {:user        (string/replace (:http://wso2.org/claims/enduser jwt) #"[^/]+/" "")
   :email       (:http://wso2.org/claims/emailaddress jwt)
   :given-name  (:http://wso2.org/claims/givenname jwt)
   :family-name (:http://wso2.org/claims/lastname jwt)
   :common-name (:http://wso2.org/claims/fullname jwt)})

(def custom-generator (jwt/generator build-custom-assertion opts))

(deftest custom-jwt-test
  (is (= user (user-from-custom-assertion (validator (custom-generator user))))))
