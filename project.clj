(defproject graaltest "0.1.0-SNAPSHOT"

  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies
  [[org.clojure/clojure "1.10.3"]

   ;; [hato "0.8.2"]
   ;; [org.clj-commons/clj-http-lite "0.4.392"]

   ;; [clj-http "3.12.0"]
   ;; [cheshire "5.10.0"]

   [org.clojure/data.json "2.4.0"]

   ]

  :main ^:skip-aot graaltest.core

  :target-path "target/%s"

  :profiles
  {:uberjar {:aot :all
             :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
