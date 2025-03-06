from utils import App, Request

spring_postgres_kotlin_app = App(8092)
spring_postgres_kotlin_app.add_payload("sql",
    safe_request=Request("/api/pets/create", body={"name": "Bobby"}),
    unsafe_request=Request("/api/pets/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "})
)
spring_postgres_kotlin_app.add_payload("command injection",
    safe_request=Request("/api/commands/execute/Johnny", method='GET'),
    unsafe_request=Request("/api/commands/execute/%27%3B%20sleep%202%3B%20%23%20", method='GET'),
)
spring_postgres_kotlin_app.add_payload("server-side request forgery",
    safe_request=Request("/api/requests/get", data_type='form', body={"url": "https://aikido.dev/"}),
    unsafe_request=Request("/api/requests/get", data_type='form', body={"url": "http://localhost:5000"})
)
spring_postgres_kotlin_app.add_payload("path traversal",
    safe_request=Request("/api/files/read", data_type='form', body={"fileName": "README.md"}),
    unsafe_request=Request("/api/files/read", data_type='form', body={"fileName": "./../databases/docker-compose.yml"})
)


spring_postgres_kotlin_app.test_all_payloads()