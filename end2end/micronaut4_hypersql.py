from utils import App, Request

micronaut4_hsql_app = App(8112)

micronaut4_hsql_app.add_payload("sql",
    safe_request=Request("/api/pets/create", body={"name": "Bobby"}),
    unsafe_request=Request("/api/pets/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "})
)

micronaut4_hsql_app.test_all_payloads()
micronaut4_hsql_app.test_blocking()
micronaut4_hsql_app.test_rate_limiting()
