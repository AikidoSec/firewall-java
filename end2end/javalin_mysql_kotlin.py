from __init__ import events
from utils import App, Request

javalin_mysql_app = App(8098)

javalin_mysql_app.add_payload(
    key="sql", test_event=events["javalin_mysql_attack"],
    safe_request=Request(route="/api/create", body={"name": "Bobby"}),
    unsafe_request=Request(route="/api/create", body={"name": "Malicious Pet\", \"Gru from the Minions\") -- "})
)

javalin_mysql_app.add_payload(
    "ssrf",
    safe_request=Request(route="/api/request", body={"url": "https://aikido.dev/"}),
    unsafe_request=Request(route="/api/request", body={"url": "http://localhost:5000"})
)

javalin_mysql_app.add_payload(
    "path_traversal",
    safe_request=Request(route="/api/read", body={"fileName": "README.md"}),
    unsafe_request=Request(route="/api/read", body={"fileName": "./../databases/docker-compose.yml"})
)
javalin_mysql_app.add_payload(
    "command_injection",
    safe_request=Request(route="/api/execute/Johnny", method='GET'),
    unsafe_request=Request(route="/api/execute/%27%3B%20sleep%202%3B%20%23%20", method='GET')
)

javalin_mysql_app.test_all_payloads()
javalin_mysql_app.test_blocking()
javalin_mysql_app.test_rate_limiting()
