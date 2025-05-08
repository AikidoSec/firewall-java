(ns RingClojurePostgres.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:dbtype "postgresql"
   :host "localhost"
   :port 5432
   :dbname "db"
   :user "user"
   :password "password"})

(defrecord Pet [pet-id name owner])
(defn get-all-pets []
  (jdbc/query db-spec ["SELECT * FROM pets"]))

(defn get-pet-by-id [id]
  (let [result (jdbc/query db-spec ["SELECT * FROM pets WHERE pet_id=?" id])]
    (if (seq result)
      (let [pet (first result)]
        (->Pet (:pet_id pet) (:pet_name pet) (:owner pet)))
      (->Pet 0 "Unknown" "Unknown"))))

(defn create-pet-by-name [pet-name]
  (let [sql (str "INSERT INTO pets (pet_name, owner) VALUES ('" pet-name "', 'Aikido Security')")]
    (jdbc/execute! db-spec [sql])))
