from __init__ import events
from utils import App

spring_boot_mysql_app = App(8082)
payloads = {
    "safe": {"name": "Bobby"},
    "unsafe": {"name": 'Malicious Pet", "Gru from the Minions") -- '}
}
spring_boot_mysql_app.add_payload(
    "sql_mysql", route="/api/pets/create",
    safe={"name": "Bobby"}, unsafe={"name": 'Malicious Pet", "Gru from the Minions") -- '},
    test_event=events["spring_mysql_boot_mysql_attack"], user="123"
)
spring_boot_mysql_app.add_payload(
    "sql_mariadb", route="/api/pets/create/mariadb",
    safe={"name": "Bobby"}, unsafe={"name": 'Malicious Pet", "Gru from the Minions") -- '},
    test_event=events["spring_mysql_boot_mariadb_attack"], user="456"
)

spring_boot_mysql_app.test_all_payloads()
spring_boot_mysql_app.test_blocking()
spring_boot_mysql_app.test_rate_limiting()
