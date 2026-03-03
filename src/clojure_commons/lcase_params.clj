(ns clojure-commons.lcase-params
  (:require
   [clojure.string :as string]
   [medley.core :as medley]))

(defn wrap-lcase-params
  "Middleware that converts all parameters to lower case so that they can be treated as effectively
   case-insensitive."
  [handler]
  (let [lc (fn [k] (-> k name string/lower-case keyword))]
    (fn [req]
      (-> req
          (update-in [:params] (partial medley/map-keys lc))
          (update-in [:query-params] (partial medley/map-keys lc))
          handler))))

(defn- lcase-query-param-value
  [value]
  (cond
    (string? value)     (string/lower-case value)
    (sequential? value) (mapv lcase-query-param-value value)
    :else               value))

(defn- lcase-query-param-values
  [target whitelist]
  (into {} (for [[k v] target]
             (if (contains? whitelist k)
               [k (lcase-query-param-value v)]
               [k v]))))

(defn wrap-lcase-query-param-values
  "Middleware that converts parameter values to lower case so that the values can be treated as effectively
   caseinsensitive. This middleware does not support nested parameter maps at this time."
  [handler whitelist]
  (let [whitelist (set (concat whitelist (map keyword whitelist)))]
    (fn [req]
      (-> req
          (update-in [:params] lcase-query-param-values whitelist)
          (update-in [:query-params] lcase-query-param-values whitelist)
          handler))))
