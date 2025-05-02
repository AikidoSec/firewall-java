(ns RingClojurePostgres.core
    (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response]]))

(defn html-handler [file]
    (let [html-content (slurp file)]
        (response html-content)))

(def app
    (fn [request]
        (case (:uri request)
            "/" (html-handler "resources/index.html")
            "/pages/create" (html-handler "resources/create.html")
            "/pages/execute" (html-handler "resources/execute_command.html")
            "/pages/request" (html-handler "resources/request.html")
            "/pages/read" (html-handler "resources/read_file.html")
            ; Test rate-limiting
            "/test_ratelimiting_1" {:body "Hello" :status 200}

            {:body "404 Not Found" :status 404})))

(defn -main
    [& args]
    (run-jetty app {:port 8102 :join? false}))
