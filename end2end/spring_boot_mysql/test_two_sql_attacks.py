import time
from utils.assert_equals import assert_eq

def test_two_sql_attacks(event_handler):
    time.sleep(5) # Wait for attack to be reported
    attacks = event_handler.fetch_attacks()

    assert_eq(len(attacks), equals=2)
    attack1 = attacks[0]["attack"]
    attack2 = attacks[1]["attack"]

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