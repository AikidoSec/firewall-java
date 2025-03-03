from __init__ import events
from utils import App, Request

javalin_postgres_app = App(8088)

javalin_postgres_app.add_payload(
    "sql",
    safe_request=Request(
        route="/api/create", body={"name": "Bobby"}
    ),
    unsafe_request=Request(
        route="/api/create", body={"name": "Malicious Pet', 'Gru from the Minions') -- "}
    ),
    test_event=events["javalin_postgres_attack"]
)
javalin_postgres_app.add_payload(
    "exec",
    safe_request=Request(route="/api/execute/Bobby", method='GET'),
    unsafe_request=Request(route="/api/execute/Malicious%20Pet%27%2C%20%27Gru%20from%20the%20Minions%27%29%20--%20", method='GET')
)
javalin_postgres_app.add_payload(
    "path_traversal_via_cookie",
    # First fpath in Cookie list gets taken : ctx.cookie(str key)
    safe_request=Request("/api/read_cookie", method='GET', headers={'Cookie': 'fpath=home.txt;fpath=123;fpath=../secrets/key.txt'}),
    unsafe_request=Request("/api/read_cookie", method='GET', headers={'Cookie': 'fpath=../secrets/key.txt;fpath=home.txt'})
)
javalin_postgres_app.add_payload(
    "path_traversal_via_cookiemap",
    # Last fpath in Cookie list gets taken : ctx.cookieMap()
    safe_request=Request("/api/read_cookiemap", method='GET', headers={'Cookie': 'fpath=../secrets/key.txt;fpath=home.txt'}),
    unsafe_request=Request("/api/read_cookiemap", method='GET', headers={'Cookie': 'fpath=home.txt;fpath=123;fpath=../secrets/key.txt'}),
)
javalin_postgres_app.add_payload(
    "path_traversal_via_cookie_all_same",
    # Last fpath in Cookie list gets taken : ctx.cookieMap()
    safe_request=Request("/api/read_cookie", method='GET', headers={'Cookie': 'fpath=home.txt;fpath=../secrets/key.txt;fpath=home.txt'}),
    unsafe_request=Request("/api/read_cookie", method='GET', headers={'Cookie': 'fpath=../secrets/key.txt;fpath=../secrets/key.txt'}),
)
javalin_postgres_app.add_payload(
    "path_traversal_via_cookiemap_all_same",
    # Last fpath in Cookie list gets taken : ctx.cookieMap()
    safe_request=Request("/api/read_cookiemap", method='GET', headers={'Cookie': 'fpath=home.txt;fpath=../secrets/key.txt;fpath=home.txt'}),
    unsafe_request=Request("/api/read_cookiemap", method='GET', headers={'Cookie': 'fpath=../secrets/key.txt;fpath=../secrets/key.txt'}),
)
javalin_postgres_app.test_all_payloads()
javalin_postgres_app.test_blocking()
javalin_postgres_app.test_rate_limiting()
