from time import sleep
from __init__ import events
from utils import App, Request

spring_boot_mysql_app = App(8082)

spring_boot_mysql_app.add_payload(
    "sql_mysql",test_event=events["spring_mysql_boot_mysql_attack"],
    safe_request=Request("/api/pets/create", headers={'user': '123'}, body={"name": "Bobby"}),
    unsafe_request=Request(
        "/api/pets/create", headers={'user': '123'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    )
)

spring_boot_mysql_app.add_payload(
    "sql_mariadb",test_event=events["spring_mysql_boot_mariadb_attack"],
    safe_request=Request("/api/pets/create/mariadb", headers={'user': '456'}, body={"name": "Bobby"}),
    unsafe_request=Request(
        "/api/pets/create/mariadb", headers={'user': '456'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    )
)

spring_boot_mysql_app.test_all_payloads()
spring_boot_mysql_app.test_blocking()
spring_boot_mysql_app.test_rate_limiting()

# Testing forceProtectionOff 1/2
# 1: True -> /api/pets/create
# 2: False -> /api/*
spring_boot_mysql_app.event_handler.set_protection(True, False)
spring_boot_mysql_app.add_payload(
    "sql_test_mysql_vs_mariadb_force_protection_off",
    safe_request=Request(
        "/api/pets/create", headers={'user': '123'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    ),
    unsafe_request=Request(
        "/api/pets/create/mariadb", headers={'user': '456'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    )
)

sleep(70) # Wait for config to be fetched
spring_boot_mysql_app.test_payload("sql_test_mysql_vs_mariadb_force_protection_off")


# Testing forceProtectionOff 2/2
# 1: False -> /api/pets/create
# 2: True -> /api/*
spring_boot_mysql_app.event_handler.set_protection(False, True)
spring_boot_mysql_app.add_payload(
    "sql_test_force_protection_off_all_mysql",
    safe_request=Request(
        "/api/pets/create", headers={'user': '123'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    )
)
spring_boot_mysql_app.add_payload(
    "sql_test_force_protection_off_all_mariadb",
    safe_request=Request(
        "/api/pets/create/mariadb", headers={'user': '456'}, body={"name": 'Malicious Pet", "Gru from the Minions") -- '}
    )
)

sleep(70) # Wait for config to be fetched
spring_boot_mysql_app.test_payload("sql_test_force_protection_off_all_mysql")
spring_boot_mysql_app.test_payload("sql_test_force_protection_off_all_mariadb")
