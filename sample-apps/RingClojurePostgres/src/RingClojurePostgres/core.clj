(ns RingClojurePostgres.core
    (:require
        [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
        [ring.middleware.params :refer [wrap-params]]
        [RingClojurePostgres.helpers :as helpers]
        [RingClojurePostgres.db :as db]
        [ring.adapter.jetty :refer [run-jetty]]
        [ring.util.response :refer [response]]))

(defn html-handler [file]
    (let [html-content (slurp file)]
        (response html-content)))

(defn get-all-pets-handler [request]
  (let [pets (db/get-all-pets)]
    (response pets)))

(defn create-pet-handler [request]
  (let [pet-name (:name (:body request)) ; Accessing the parsed JSON body directly
        rows-created (db/create-pet-by-name pet-name)]
    (response (str rows-created))))

(defn execute-command-handler [request]
  (let [user-command (:userCommand (:body request))
        result (helpers/execute-shell-command user-command)]
    (response result)))

(defn execute-command-path-handler [request]
  (let [user-command (get-in request [:path-params :command])
        result (helpers/execute-shell-command user-command)]
    (response result)))

(defn make-http-request-handler [request]
  (let [url (:url (:body request))] ; Accessing the parsed JSON body directly
    (if url
      (let [response-body (helpers/make-http-request url)] ; Call the helper function
        {:body response-body}) ; Return the response body
      {:status 400
       :body {:error "Missing URL"}}))) ; Handle missing URL case

(defn make-http-request-clj-client-handler [request]
  (let [url (:url (:body request))] ; Accessing the parsed JSON body directly
    (if url
      (let [response-body (helpers/make-http-request-with-clj-client url)] ; Call the helper function
        {:body response-body}) ; Return the response body
      {:status 400
       :body {:error "Missing URL"}}))) ; Handle missing URL case

(defn read-file-handler [request]
  (let [file-path (get-in request [:query-params "path"])
        content (helpers/read-file file-path)]
    (response content)))

(defn read-cookie-handler [request]
  (let [file-path (get-in request [:cookies "fpath"])
        content (helpers/read-file file-path)]
    (response content)))

(defn read-cookie-map-handler [request]
  (let [file-path (get-in request [:cookies "fpath"])
        content (helpers/read-file file-path)]
    (response content)))

(def app
    (fn [request]
        (case (:uri request)
            "/" (html-handler "resources/index.html")
            "/pages/create" (html-handler "resources/create.html")
            "/pages/execute" (html-handler "resources/execute_command.html")
            "/pages/request" (html-handler "resources/request.html")
            "/pages/read" (html-handler "resources/read_file.html")
            "/api/pets/" (get-all-pets-handler request)
            "/api/create" (create-pet-handler request)
            "/api/execute" (execute-command-handler request)
            (re-find #"/api/execute/.*" (:uri request)) (execute-command-path-handler request)
            "/api/request" (make-http-request-handler request)
            "/api/request2" (make-http-request-clj-client-handler request)
            "/api/read" (read-file-handler request)
            "/api/read_cookie" (read-cookie-handler request)
            "/api/read_cookiemap" (read-cookie-map-handler request)
            ; Test rate-limiting
            "/test_ratelimiting_1" {:body "Hello" :status 200}

            {:body "404 Not Found" :status 404})))
(def app
  (wrap-json-body app {:keywords? true :bigdecimals? true}))
(def app
  (wrap-params app))
(def app
    (wrap-json-response app))

(defn -main
    [& args]
    (run-jetty app {:port 8102 :join? false}))
