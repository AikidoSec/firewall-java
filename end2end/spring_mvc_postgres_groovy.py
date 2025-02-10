from utils import App

spring_postgres_groovy_app = App(8094)
spring_postgres_groovy_app.add_payload(
    "SQL", route="/api/pets/create",
    safe={"name": "Bobby"}, unsafe={"name": "Malicious Pet', 'Gru from the Minions') -- "}
)
spring_postgres_groovy_app.add_payload(
    "Command Injection", route="/api/commands/execute/", pathvar=True,
    safe="Johnny", unsafe="'; sleep 2; # "
)
spring_postgres_groovy_app.add_payload(
    "SSRF", route="/api/requests/get", json=False,
    safe={"url": "https://aikido.dev/"}, unsafe={"url": "http://localhost:5000"}
)
spring_postgres_groovy_app.add_payload(
    "Path Traversal", route="/api/files/read", json=False,
    safe={"fileName": "README.md"}, unsafe={"fileName": "./../databases/docker-compose.yml"}
)

spring_postgres_groovy_app.test_all_payloads()
