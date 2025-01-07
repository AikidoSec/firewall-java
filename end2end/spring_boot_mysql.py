from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads
from spring_boot_mysql.test_two_sql_attacks import test_two_sql_attacks
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
test_safe_vs_unsafe_payloads(payloads, urls) # Test MySQL driver
test_safe_vs_unsafe_payloads(payloads, urls, "/mariadb") # Also test MariaDB driver
test_two_sql_attacks(event_handler)