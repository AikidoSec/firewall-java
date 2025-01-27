from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads, test_payloads_path_variables
from javalin_mysql_kotlin.test_sql_attacks import test_sql_attack
from javalin_postgres.test_ip_blocking import test_ip_blocking
from javalin_postgres.test_bot_blocking import test_bot_blocking
from javalin_postgres.test_ratelimiting import test_ratelimiting_per_user, test_ratelimiting
from utils.EventHandler import EventHandler

payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": "Malicious Pet\", \"Gru from the Minions\") -- " }
}
payloads_exec = {
    "safe": "Johhny",
    "unsafe": "'; sleep 2; # "
}
payloads_ssrf = {
    "safe": { "url": "https://aikido.dev/" },
    "unsafe": { "url": "http://localhost:5000" },
    "json": False,
}
payloads_path_traversal = {
    "safe": { "fileName": "README.md" },
    "unsafe": { "fileName": "./../databases/docker-compose.yml" },
    "json": False,
}
urls = {
    "disabled": "http://localhost:8099",
    "enabled": "http://localhost:8098"
}

event_handler = EventHandler()
event_handler.reset()
test_safe_vs_unsafe_payloads(payloads, urls, route="/api/create") # Test MySQL+Javalin compat
print("✅ Tested safe/unsafe payloads on /api/create")

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

test_sql_attack(event_handler)
print("✅ Tested accurate reporting of an attack")

# Test path variables :
test_payloads_path_variables(payloads_exec, urls, route="/api/execute/")
print("✅ Tested attack using path variables.")

# Test SSRF :
test_safe_vs_unsafe_payloads(payloads_ssrf, urls, route="/api/request")
print("✅ SSRF Tested")

# Test path traversal :
test_safe_vs_unsafe_payloads(payloads_path_traversal, urls, route="/api/read")
print("✅ Path Traversal Tested")