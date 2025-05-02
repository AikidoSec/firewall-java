(defproject RingClojurePostgres "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.11.1"]
                   [ring/ring-core "1.7.1"]
                   [ring/ring-jetty-adapter "1.7.1"]
                   [clj-http "3.12.3"]
                   [org.clojure/java.jdbc "0.7.12"]
                   [org.postgresql/postgresql "42.2.20"]
                   [ring/ring-json "0.5.1"]]
    :main ^:skip-aot RingClojurePostgres.core
    :target-path "target/%s"
    :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
