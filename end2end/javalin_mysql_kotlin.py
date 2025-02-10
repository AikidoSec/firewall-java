from javalin_mysql_kotlin.test_sql_attacks import test_sql_attack
from javalin_postgres.test_ip_blocking import test_ip_blocking
from javalin_postgres.test_bot_blocking import test_bot_blocking
from javalin_postgres.test_ratelimiting import test_ratelimiting_per_user, test_ratelimiting
from utils.app import App
javalin_mysql_app = App(8098)

javalin_mysql_app.add_payload(
    "sql", route="/api/create",
    safe={ "name":"Bobby" }, unsafe={ "name": "Malicious Pet\", \"Gru from the Minions\") -- " },
    test_event=test_sql_attack
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

# Test blocklists :
test_ip_blocking("http://localhost:8098/")
print("✅ Tested IP Blocking")
test_bot_blocking("http://localhost:8098/")
print("✅ Tested bot blocking")

# Test ratelimiting (we can use a header to set user) :
test_ratelimiting("http://localhost:8098/test_ratelimiting_1")
print("✅ Tested rate-limiting")
test_ratelimiting_per_user("http://localhost:8098/test_ratelimiting_1")
print("✅ Tested rate-limiting per user")