from utils import App

spring_webflux_postgres_app = App(8090)
spring_webflux_postgres_app.add_payload(
    "SQL", route="/api/pets/create",
    safe={"name": "Bobby"}, unsafe={"name": "Malicious Pet', 'Gru from the Minions') -- "}
)
spring_webflux_postgres_app.add_payload(
    "Command Injection", route="/api/commands/execute/", pathvar=True,
    safe="Johnny", unsafe="'; sleep 2; # "
)

spring_webflux_postgres_app.test_all_payloads()
spring_webflux_postgres_app.test_blocking()
spring_webflux_postgres_app.test_rate_limiting()
