from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads, test_payloads_path_variables

payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": "Malicious Pet', 'Gru from the Minions') -- " }
}
payloads_exec = {
    "safe": "Johhny",
    "unsafe": "'; sleep 2; # "
}
urls = {
    "disabled": "http://localhost:8095",
    "enabled":  "http://localhost:8094"
}

test_safe_vs_unsafe_payloads(payloads, urls, route="/api/pets/create") # This makes 4 requests and asserts their status codes
# Test if it can block attacks coming from a path variable:
test_payloads_path_variables(payloads_exec, urls, route="/api/commands/execute/")