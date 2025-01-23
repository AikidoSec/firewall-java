from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads
from spring_boot_mysql.test_two_sql_attacks import test_two_sql_attacks
from spring_boot_mysql.test_ip_blocking import test_ip_blocking
from spring_boot_mysql.test_bot_blocking import test_bot_blocking
from spring_boot_mysql.test_ratelimiting import test_ratelimiting_per_user, test_ratelimiting
from utils.EventHandler import EventHandler

payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": 'Malicious Pet", "Gru from the Minions") -- ' }
}
urls = {
    "disabled": "http://localhost:8083/api/pets/create",
    "enabled": "http://localhost:8082/api/pets/create"
}

event_handler = EventHandler()
event_handler.reset()
test_safe_vs_unsafe_payloads(payloads, urls, user_id="123") # Test MySQL driver
test_safe_vs_unsafe_payloads(payloads, urls, "/mariadb", user_id="456") # Also test MariaDB driver

# Test blocklists :
test_ip_blocking("http://localhost:8082/")
test_bot_blocking("http://localhost:8082/")

# Test ratelimiting (we can use a header to set user) :
test_ratelimiting("http://localhost:8082/test_ratelimiting_1")
test_ratelimiting_per_user("http://localhost:8082/test_ratelimiting_1")

test_two_sql_attacks(event_handler)