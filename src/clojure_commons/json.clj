(ns clojure-commons.json
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :refer [reader]]))

;;; Things to make working with POSTs/PUTs easier
(defonce json-mime-type "application/json")

(defn string->json
  "Parses a JSON string."
  ([s]
     (string->json s true))
  ([s keywordize?]
     (cheshire/decode s keywordize?)))

(defn body->json
  "Takes in input from a post body and slurps/parses it."
  ([body]
     (body->json body true))
  ([body keywordize?]
     (cheshire/decode-stream (reader body) keywordize?)))
