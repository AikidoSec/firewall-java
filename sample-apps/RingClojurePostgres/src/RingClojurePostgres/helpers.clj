(ns RingClojurePostgres.helpers
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as client])) ; For HTTP requests

(defn execute-shell-command [command]
  (let [output (StringBuilder.)]
      (let [process (.exec (Runtime/getRuntime) command)
            reader (io/reader (.getInputStream process))]
        (with-open [r reader]
          (doseq [line (line-seq r)]
            (.append output line)
            (.append output "\n")))
        (.waitFor process))
      (str output)))

(defn make-http-request [url-string]
  (let [response (StringBuilder.)]
      (let [url (java.net.URL. url-string)
            conn (.openConnection url)]
        (.setRequestMethod conn "GET")
        (with-open [in (io/reader (.getInputStream conn))]
          (doseq [line (line-seq in)]
            (.append response line)
            (.append response "\n")))
        (str response))))

(defn read-file [file-path]
  (let [content (StringBuilder.)
        file (io/file (str "resources/blogs/" file-path))]
      (with-open [br (io/reader file)]
        (doseq [line (line-seq br)]
          (.append content line)
          (.append content "\n")))
      (str content)))


(defn make-http-request-with-clj-client [url-string]
  (let [response (StringBuilder.)]
    (try
      (let [resp (client/get url-string)]
        (when (:body resp)
          (.append response (:body resp)))
        (str response)) ; Return the response as a string
      (catch Exception e
        (str "Error occurred: " (.getMessage e))) ; Handle exceptions
      )))
