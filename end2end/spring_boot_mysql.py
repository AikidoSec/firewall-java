import time
from utils.test_safe_vs_unsafe_payloads import test_safe_vs_unsafe_payloads
from utils.check_events_from_mock import fetch_events_from_mock, filter_on_event_type
from utils.assert_equals import assert_eq

payloads = {
    "safe": { "name": "Bobby" },
    "unsafe": { "name": 'Malicious Pet", "Gru from the Minions") -- ' }
}
urls = {
    "disabled": "http://localhost:8083/api/pets/create",
    "enabled": "http://localhost:8082/api/pets/create"
}

# Now test the reporting of the attacks
def test_attacks_detected():
    time.sleep(3) # Wait for attack to be reported (max 2 seconds, 1 second of margin)
    attacks = filter_on_event_type(fetch_events_from_mock("http://localhost:5000"), "detected_attack")

    assert len(attacks) == 2
    attack1 = attacks[0]["attack"]
    attack2 = attacks[1]["attack"]
    print(attacks[0]["attack"])
    # Test both attacks together :
    assert_eq(val1=attack1["blocked"], val2=attack2["blocked"], equals=True)
    assert_eq(val1=attack1["kind"], val2=attack2["kind"], equals="sql_injection")
    assert_eq(val1=attack1["metadata"], val2=attack2["metadata"],
            equals={'sql': 'INSERT INTO pets (pet_name, owner) VALUES ("Malicious Pet", "Gru from the Minions") -- ", "Aikido Security")'})
    assert_eq(val1=attack1["path"], val2=attack2["path"], equals='.name')
    assert_eq(val1=attack1["payload"], val2=attack2["payload"], equals='Malicious Pet", "Gru from the Minions") -- ')
    assert_eq(val1=attack1["source"], val2=attack2["source"], equals="body")
    # Different :
    assert_eq(attack1["operation"], equals="(MySQL Connector/J) java.sql.Connection.prepareStatement")
    assert_eq(attack2["operation"], equals="(MariaDB Connector/J) java.sql.Connection.prepareStatement")

test_safe_vs_unsafe_payloads(payloads, urls) # Test MySQL driver
test_safe_vs_unsafe_payloads(payloads, urls, "/mariadb") # Also test MariaDB driver
test_attacks_detected()