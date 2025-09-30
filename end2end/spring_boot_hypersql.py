from utils import App, Request

spring_boot_hsql_app = App(8108)

spring_boot_hsql_app.add_payload("sql",
    safe_request=Request("/api/pets/create", body={"name": "Bobby"}),
    unsafe_request=Request("/api/pets/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "})
)

spring_boot_hsql_app.test_all_payloads()
