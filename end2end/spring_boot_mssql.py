from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads

payloads = {
    "safe": {"name": "Bobby"},
    "unsafe": {"name": "Malicious Pet', 'Gru from the Minions'); -- "}
}
urls = {
    "disabled": "http://localhost:8087/api/pets/create",
    "enabled": "http://localhost:8086/api/pets/create"
}

test_safe_vs_unsafe_payloads(payloads, urls)  # This makes 4 requests and asserts their status codes
