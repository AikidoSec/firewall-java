from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads, test_payloads_path_variables
from spring_boot_mysql.test_bot_blocking import test_bot_blocking
from spring_boot_mysql.test_ratelimiting import test_ratelimiting_per_user, test_ratelimiting
from spring_boot_mysql.test_ip_blocking import test_ip_blocking
payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": "Malicious Pet', 'Gru from the Minions') -- " }
}
payloads_exec = {
    "safe": "Johhny",
    "unsafe": "'; sleep 2; # "
}
urls = {
    "disabled": "http://localhost:8091",
    "enabled":  "http://localhost:8090"
}

test_safe_vs_unsafe_payloads(payloads, urls, route="/api/pets/create")
print("✅ Tested safe/unsafe payloads on /api/create")

# Test blocklists (WIP -> Writing response support)
#test_ip_blocking("http://localhost:8090/")
#print("✅ Tested IP Blocking")
#test_bot_blocking("http://localhost:8090/")
#print("✅ Tested bot blocking")

# Test ratelimiting (we can use a header to set user) :
test_ratelimiting("http://localhost:8090/test_ratelimiting_1")
print("✅ Tested rate-limiting")
test_ratelimiting_per_user("http://localhost:8090/test_ratelimiting_1")
print("✅ Tested rate-limiting per user")

# Test path variables : (WIP -> Path Variable support)
test_payloads_path_variables(payloads_exec, urls, route="/api/commands/execute/")
print("✅ Tested attack using path variables.")
