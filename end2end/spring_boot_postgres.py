from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads, test_payloads_path_variables

payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": "Malicious Pet', 'Gru from the Minions') -- " }
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
    "safe": { "fileName": ".gitignore" },
    "unsafe": { "fileName": "./../databases/docker-compose.yml" },
    "json": False,
}
urls = {
    "disabled": "http://localhost:8081",
    "enabled":  "http://localhost:8080"
}

test_safe_vs_unsafe_payloads(payloads, urls, route="/api/pets/create") # This makes 4 requests and asserts their status codes
print("✅ SQL Tested")
# Test if it can block attacks coming from a path variable:
test_payloads_path_variables(payloads_exec, urls, route="/api/commands/execute/")
print("✅ Path variables and Command Injection Tested")

# Test SSRF :
test_safe_vs_unsafe_payloads(payloads_ssrf, urls, route="/api/requests/get") # This makes 4 requests and asserts their status codes
print("✅ SSRF Tested")

# Test path traversal :
test_safe_vs_unsafe_payloads(payloads_path_traversal, urls, route="/api/files/read") # This makes 4 requests and asserts their status codes
print("✅ Path Traversal Tested")
