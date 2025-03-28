from utils import App, Request

spring_boot_mssql_app = App(8086)
spring_boot_mssql_app.add_payload(
    "sql",
    safe_request=Request(route="/api/pets/create", body={"name": "Bobby"}),
    unsafe_request=Request(route="/api/pets/create", body={"name": "Malicious Pet', 'Gru from the Minions'); -- "})
)

spring_boot_mssql_app.test_all_payloads()
spring_boot_mssql_app.test_blocking()
