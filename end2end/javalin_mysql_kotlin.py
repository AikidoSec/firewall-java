from utils import App
from __init__ import events

javalin_mysql_app = App(8098)
javalin_mysql_app.add_payload(
    "sql", route="/api/create",
    safe={ "name":"Bobby" }, unsafe={ "name": "Malicious Pet\", \"Gru from the Minions\") -- " },
    test_event=events["javalin_mysql_attack"]
)
javalin_mysql_app.add_payload(
    "ssrf", route="/api/request",
    safe={ "url": "https://aikido.dev/" }, unsafe={ "url": "http://localhost:5000" }
)
javalin_mysql_app.add_payload(
    "path_traversal", route="/api/read",
    safe={ "fileName": "README.md" }, unsafe={ "fileName": "./../databases/docker-compose.yml" }
)
javalin_mysql_app.add_payload(
    "command_injection", route="/api/execute/",
    safe="Johnny", unsafe="'; sleep 2; # ", pathvar=True
)

javalin_mysql_app.test_all_payloads()
javalin_mysql_app.test_blocking()
javalin_mysql_app.test_rate_limiting()