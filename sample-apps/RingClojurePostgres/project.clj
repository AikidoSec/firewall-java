(defproject RingClojurePostgres "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.11.1"]
                   [ring/ring-core "1.15.3"]
                   [ring/ring-jetty-adapter "1.15.3"]
                   [org.eclipse.jetty.toolchain/jetty-jakarta-servlet-api "5.0.2"]
                   [clj-http "3.12.3"]
                   [org.clojure/java.jdbc "0.7.12"]
                   [org.postgresql/postgresql "42.2.20"]
                   [ring/ring-json "0.5.1"]]
    :main ^:skip-aot RingClojurePostgres.core
    :target-path "target/%s"
    :jvm-opts ~(if-let [agent (System/getenv "AIKIDO_AGENT_JAR")] [(str "-javaagent:" agent)] [])
    :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
