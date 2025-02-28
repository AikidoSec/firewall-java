from __init__ import events
from utils import App, Request

javalin_postgres_app = App(8100)

javalin_postgres_app.add_payload_2(
    "sql",
    safe_request=Request(
        route="/api/create", body={"name": "Bobby"}
    ),
    unsafe_request=Request(
        route="/api/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "}
    ),
    test_event=events["javalin_postgres_attack"]
)
javalin_postgres_app.add_payload_2(
    "exec",
    safe_request=Request(route="/api/execute/Bobby", method='GET'),
    unsafe_request=Request(route="/api/execute/Malicious%20Pet%27%2C%20%27Gru%20from%20the%20Minions%27%29%20--%20", method='GET')
)

javalin_postgres_app.test_all_payloads()
javalin_postgres_app.test_blocking()
javalin_postgres_app.test_rate_limiting()
