import time
import requests

from .EventHandler import EventHandler
from .assert_equals import assert_eq
from .request import Request
from .test_bot_blocking import test_bot_blocking
from .test_ip_blocking import test_ip_blocking
from .test_ratelimiting import test_ratelimiting, test_ratelimiting_per_user
from .test_payloads_safe_vs_unsafe import test_payloads_safe_vs_unsafe

class App:
    def __init__(self, port):
        self.urls = {
            "enabled": f"http://localhost:{port}",
            "disabled": f"http://localhost:{port + 1}"
        }
        self.payloads = {}
        self.event_handler = EventHandler()
        if not wait_until_live(self.urls["enabled"]):
            raise Exception(self.urls["enabled"] + " is not turning on.")
        if not wait_until_live(self.urls["disabled"]):
            raise Exception(self.urls["disabled"] + " is not turning on.")

    def add_payload(self,key, safe_request, unsafe_request=None, test_event=None):
        self.payloads[key] = {
            "safe": safe_request,
            "unsafe": unsafe_request,
            "test_event": test_event
        }

    def test_payload(self, key):
        if key not in self.payloads:
            raise Exception("Payload " + key + " not found.")
        payload = self.payloads.get(key)

        self.event_handler.reset()
        test_payloads_safe_vs_unsafe(payload, self.urls)
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

def wait_until_live(url):
    while i in range(10):
        res = requests.get(url)
        if res.status_code == 200:
            return True
        time.sleep(2)
    return False
