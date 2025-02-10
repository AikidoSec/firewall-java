import time

from .EventHandler import EventHandler
from .assert_equals import assert_eq
from .test_bot_blocking import test_bot_blocking
from .test_ip_blocking import test_ip_blocking
from .test_ratelimiting import test_ratelimiting, test_ratelimiting_per_user
from .test_safe_vs_unsafe_payloads import test_payloads_path_variables, test_safe_vs_unsafe_payloads


class App:
    def __init__(self, port):
        self.urls = {
            "enabled": f"http://localhost:{port}",
            "disabled": f"http://localhost:{port + 1}"
        }
        self.payloads = {}
        self.event_handler = EventHandler()

    def add_payload(self, key, route, safe, unsafe, pathvar=False, test_event=None, user=None):
        self.payloads[key] = {
            "route": route,
            "safe": safe, "unsafe": unsafe,
            "pathvar": pathvar,
            "test_event": test_event,
            "user": user
        }

    def test_payload(self, key):
        if key not in self.payloads:
            raise Exception("Payload " + key + " not found.")
        payload = self.payloads.get(key)

        self.event_handler.reset()
        if payload["pathvar"] is True:
            test_payloads_path_variables(payload, self.urls, payload["route"])
        else:
            test_safe_vs_unsafe_payloads(payload, self.urls, payload["route"], user_id=payload["user"])
        print("✅ Tested payload: " + key)

        if payload["test_event"]:
            time.sleep(5)
            attacks = self.event_handler.fetch_attacks()
            assert_eq(len(attacks), equals=1)
            for k, v in payload["test_event"].items():
                if k == "user_id":  # exemption rule for user ids
                    assert_eq(attacks[0]["attack"]["user"]["id"], v)
                else:
                    assert_eq(attacks[0]["attack"][k], equals=v)
            print("✅ Tested accurate event reporting for: " + key)

    def test_all_payloads(self):
        for key in self.payloads.keys():
            self.test_payload(key)

    def test_blocking(self):
        test_bot_blocking(self.urls["enabled"])
        print("✅ Tested bot blocking")
        test_ip_blocking(self.urls["enabled"])
        print("✅ Tested IP Blocking")

    def test_rate_limiting(self, route="/test_ratelimiting_1"):
        test_ratelimiting(self.urls["enabled"] + route)
        print("✅ Tested rate-limiting")
        test_ratelimiting_per_user(self.urls["enabled"] + route)
        print("✅ Tested rate-limiting (User)")
