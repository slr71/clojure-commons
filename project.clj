(defproject org.cyverse/clojure-commons "3.0.13-SNAPSHOT"
  :description "Common Utilities for Clojure Projects"
  :url "https://github.com/cyverse-de/clojure-commons"
  :license {:name "BSD"
            :url "https://cyverse.org/license"}
  :eastwood {:exclude-namespaces [:test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]}
  :plugins [[jonase/eastwood "1.4.3"]
            [lein-ancient "1.0.0"]
            [test2junit "1.4.4"]]
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  ;; Fail the build on a new dependency conflict rather than printing a
  ;; warning nobody reads.
  :pedantic? :abort
  ;; Everything except the jackson-* group records a version Leiningen already
  ;; resolves, read off the resolved classpath rather than copied from lein's
  ;; "Consider using these :managed-dependencies" hint -- that hint names the
  ;; version that LOST the conflict, so pasting it would be a silent upgrade.
  ;; These four arbitrate compojure-api 1.1.14's internally inconsistent tree
  ;; (it is the final release of an archived project) and clj-http vs buddy-core.
  ;;
  ;; The jackson-* entries fix a pre-existing split: on main, annotations and
  ;; databind were at 2.18.3 while core/cbor/smile were at 2.20.0. Jackson needs
  ;; those to move together -- a mismatch surfaces as NoSuchMethodError at
  ;; runtime, and :pedantic? cannot see it because each artifact is individually
  ;; unambiguous. cheshire 6.2.0 brings core/cbor/smile at 2.21.1, so databind
  ;; and annotations are aligned to 2.21 to match.
  :managed-dependencies [[com.fasterxml.jackson.core/jackson-annotations "2.21"]
                         [com.fasterxml.jackson.core/jackson-databind "2.21.1"]
                         [commons-codec "1.16.1"]
                         [prismatic/schema "1.1.12"]
                         [ring/ring-codec "1.1.0"]
                         [ring/ring-core "1.6.3"]]
  :dependencies [[org.clojure/clojure "1.12.5"]
                 [org.clojure/tools.logging "1.3.1"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [metosin/compojure-api "1.1.14"]
                 [cheshire "6.2.0"]
                 [clj-http "3.13.1"]
                 [clj-time "0.15.2"]
                 [clojurewerkz/propertied "1.3.0"]
                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [commons-configuration "1.10"    ; provides org.apache.commons.configuration
                  :exclusions [commons-logging]]
                 [io.github.clj-kondo/config-slingshot-slingshot "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [dev.weavejester/medley "1.10.0"]
                 [slingshot "0.12.2"]
                 [trptcolin/versioneer "0.2.0"]
                 [org.cyverse/service-logging "2.8.6"]]
  ;; lein-clj-kondo lives in its own profile because its dependency tree is
  ;; internally inconsistent -- clj-kondo pulls Clojure 1.11.4 while its own sci
  ;; dependency pulls 1.12.0 -- which trips :pedantic? :abort on a conflict that
  ;; exists entirely inside a third-party plugin and never reaches the runtime
  ;; classpath. Lint with `lein with-profile +kondo clj-kondo`.
  :profiles {:kondo {:plugins [[com.github.clj-kondo/lein-clj-kondo "2026.08.04"]]
                     :pedantic? :warn}
             :test {:resource-paths ["resources" "test-resources"]}})
