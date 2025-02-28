from utils import App, Request

spring_webflux_postgres_app = App(8090)

spring_webflux_postgres_app.add_payload("sql",
    safe_request=Request("/api/pets/create", body={"name": "Bobby"}),
    unsafe_request=Request("/api/pets/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "})
)
spring_webflux_postgres_app.add_payload("command injection",
    safe_request=Request("/api/commands/execute/Johnny", method='GET'),
    unsafe_request=Request("/api/commands/execute/%27%3B%20sleep%202%3B%20%23%20", method='GET'),
)

spring_webflux_postgres_app.test_all_payloads()
spring_webflux_postgres_app.test_blocking()
spring_webflux_postgres_app.test_rate_limiting()
