(defproject RingClojurePostgres "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.11.1"]
                   [ring/ring-core "1.7.1"]
                   [ring/ring-jetty-adapter "1.7.1"]]
    :main ^:skip-aot RingClojurePostgres.core
    :target-path "target/%s"
    :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
