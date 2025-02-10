from __init__ import events
from utils import App

javalin_postgres_app = App(8088)
javalin_postgres_app.add_payload(
    "sql", route="/api/create",
    safe={"name": "Bobby"}, unsafe={"name": "Malicious Pet', 'Gru from the Minions') -- "},
    test_event=events["javalin_postgres_attack"]
)
javalin_postgres_app.add_payload(
    "exec", route="/api/execute/",
    safe="Bobby", unsafe="Malicious Pet', 'Gru from the Minions') -- ",
    pathvar=True
)

javalin_postgres_app.test_all_payloads()
javalin_postgres_app.test_blocking()
javalin_postgres_app.test_rate_limiting()
