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

# Test that spring handles cookies correctly: 
spring_webflux_postgres_app.add_payload(
    "path_traversal_via_cookie_singular",
    # First command in Cookie list gets used.
    safe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=john'}),
    unsafe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=%27%3B%20sleep%202%3B%20%23%20'})
)
spring_webflux_postgres_app.add_payload(
    "path_traversal_via_cookie",
    # First command in Cookie list gets used.
    safe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=john;command=123;command=%27%3B%20sleep%202%3B%20%23%20'}),
    unsafe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=%27%3B%20sleep%202%3B%20%23%20;command=john'})
)
spring_webflux_postgres_app.add_payload(
    "path_traversal_via_cookie_all_same",
    safe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=john;command=%27%3B%20sleep%202%3B%20%23%20;command=john'}),
    unsafe_request=Request("/api/commands/executeFromCookie", method='GET', headers={'Cookie': 'command=%27%3B%20sleep%202%3B%20%23%20;command=%27%3B%20sleep%202%3B%20%23%20'}),
)

spring_webflux_postgres_app.test_all_payloads()
spring_webflux_postgres_app.test_blocking()
spring_webflux_postgres_app.test_rate_limiting()
