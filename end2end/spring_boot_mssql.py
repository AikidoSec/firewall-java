from utils import App

spring_boot_mssql_app = App(8086)
spring_boot_mssql_app.add_payload(
    "sql", route="/api/pets/create",
    safe={"name": "Bobby"}, unsafe={"name": "Malicious Pet', 'Gru from the Minions'); -- "}
)

spring_boot_mssql_app.test_all_payloads()
spring_boot_mssql_app.test_blocking()
