from utils import App, Request

ring_clojure_postgres_app = App(8102)

ring_clojure_postgres_app.add_payload("sql",
    safe_request=Request("/api/create", body={"name": "Bobby"}),
    unsafe_request=Request("/api/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "})
)

ring_clojure_postgres_app.test_all_payloads()
