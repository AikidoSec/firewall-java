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

spring_boot_mysql_app.add_payload(
    "path_traversal_via_cookie",
    # First fpath in Cookie list gets used.
    safe_request=Request("/api/files/read_cookie", method='GET', headers={'Cookie': 'fpath=Makefile;fpath=123;fpath=../databases/docker-compose.yml'}),
    unsafe_request=Request("/api/files/read_cookie", method='GET', headers={'Cookie': 'fpath=../databases/docker-compose.yml;fpath=Makefile'})
)
spring_boot_mysql_app.add_payload(
    "path_traversal_via_cookie_all_same",
    safe_request=Request("/api/files/read_cookie", method='GET', headers={'Cookie': 'fpath=Makefile;fpath=../databases/docker-compose.yml;fpath=Makefile'}),
    unsafe_request=Request("/api/files/read_cookie", method='GET', headers={'Cookie': 'fpath=../databases/docker-compose.yml;fpath=../databases/docker-compose.yml'}),
)

spring_boot_mysql_app.test_all_payloads()
spring_boot_mysql_app.test_blocking()
spring_boot_mysql_app.test_rate_limiting()
