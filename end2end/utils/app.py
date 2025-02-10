from .test_safe_vs_unsafe_payloads import test_payloads_path_variables, test_safe_vs_unsafe_payloads
from .EventHandler import EventHandler


class App:
    def __init__(self, port):
        self.urls = {
            "enabled": f"http://localhost:{port}",
            "disabled": f"http://localhost:{port+1}"
        }
        self.payloads = {}
        self.event_handler = EventHandler()

    def add_payload(self, key, route, safe, unsafe, pathvar=False, test_event=None):
        self.payloads[key] = {
            "route": route,
            "safe": safe, "unsafe": unsafe,
            "pathvar": pathvar,
            "test_event": test_event
        }

    def test_payload(self, key):
        if key not in self.payloads:
            raise Exception("Payload " + key + " not found.")
        payload = self.payloads.get(key)

        self.event_handler.reset()
        if payload["pathvar"] is True:
            test_payloads_path_variables(payload, self.urls, payload["route"])
        else:
            test_safe_vs_unsafe_payloads(payload, self.urls, payload["route"])
        print("✅ Tested payload: " + key)

        if payload["test_event"]:
            payload["test_event"](self.event_handler)
            print("✅ Tested accurate event reporting for: " + key)

    def test_all_payloads(self):
        for key in self.payloads.keys():
            self.test_payload(key)
