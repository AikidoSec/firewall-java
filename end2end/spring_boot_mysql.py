import time
from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads
from spring_boot_mysql.test_two_sql_attacks import test_two_sql_attacks
from spring_boot_mysql.test_ip_blocking import test_ip_blocking
from spring_boot_mysql.test_bot_blocking import test_bot_blocking
from spring_boot_mysql.test_ratelimiting import test_ratelimiting_per_user, test_ratelimiting
from utils.EventHandler import EventHandler
from utils.make_requests import make_post_request

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

# Test SQL attacks :
test_safe_vs_unsafe_payloads(payloads, urls, user_id="123") # Test MySQL driver
print("✅ MySQL Driver tested")

test_safe_vs_unsafe_payloads(payloads, urls, "/mariadb", user_id="456") # Also test MariaDB driver
print("✅ MariaDB Driver tested")

# Test blocklists :
test_ip_blocking("http://localhost:8082/")
print("✅ IP Blocking tested")

test_bot_blocking("http://localhost:8082/")
print("✅ Bot Blocking tested")


# Test ratelimiting (we can use a header to set user) :
test_ratelimiting("http://localhost:8082/test_ratelimiting_1")
print("✅ Rate-limiting tested (IP Based)")

test_ratelimiting_per_user("http://localhost:8082/test_ratelimiting_1")
print("✅ Rate-limiting tested (User Based)")

test_two_sql_attacks(event_handler)
print("✅ Attack reporting tested (2x)")

# Test forceProtectionOff
make_post_request(urls["enabled"], payloads["unsafe"], status_code=500)

event_handler.set_protection(True, False)
time.sleep(70) # Wait for config to be fetched
make_post_request(urls["enabled"], payloads["unsafe"], status_code=200)

event_handler.set_protection(False, True)
time.sleep(70) # Wait for config to be fetched
make_post_request(urls["enabled"], payloads["unsafe"], status_code=200)

print("✅ Tested force protection off")
