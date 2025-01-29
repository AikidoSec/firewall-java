import time
from utils.assert_equals import assert_eq


def test_sql_attack(event_handler):
    time.sleep(5)  # Wait for attack to be reported
    attacks = event_handler.fetch_attacks()

    assert_eq(len(attacks), equals=1)
    attack = attacks[0]["attack"]

    # Test both attacks together :
    assert_eq(val1=attack["blocked"], equals=True)
    assert_eq(val1=attack["kind"], equals="sql_injection")
    assert_eq(val1=attack["metadata"],
              equals={
                  'sql': "INSERT INTO pets (pet_name, owner) VALUES ('Malicious Pet', 'Gru from the Minions') -- ', 'Aikido Security')"})
    assert_eq(val1=attack["path"], equals='.name')
    assert_eq(val1=attack["payload"], equals="Malicious Pet', 'Gru from the Minions') -- ")
    assert_eq(val1=attack["source"], equals="body")
    assert_eq(attack["operation"], equals="(PostgreSQL JDBC Driver) java.sql.Connection.prepareStatement")
