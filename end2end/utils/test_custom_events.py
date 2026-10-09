import time
import requests
from utils.assert_equals import assert_eq
from utils.EventHandler import filter_on_event_type

def wait_for_custom_events(event_handler, timeout=10):
    deadline = time.time() + timeout
    while time.time() < deadline:
        custom_events = filter_on_event_type(event_handler.fetch_events_from_mock(), "custom")
        if custom_events:
            return custom_events
        time.sleep(0.5)
    return []

def test_custom_events(url, event_handler):
    event_handler.reset()

    res = requests.post(url + "/api/track", headers={
        "X-Forwarded-For": "203.0.113.7",
        "User-Agent": "zen-e2e",
    })
    assert_eq(res.status_code, equals=200)

    custom_events = wait_for_custom_events(event_handler)
    assert_eq(len(custom_events), equals=1)
    event = custom_events[0]
    assert_eq(event["name"], equals="user.login_failed")
    assert_eq(event["request"]["ipAddress"], equals="203.0.113.7")
    assert_eq(event["request"]["userAgent"], equals="zen-e2e")
    assert_eq(event["request"]["method"], equals="POST")
    assert_eq(event["request"]["route"], equals="/api/track")
    assert_eq(event["agent"]["library"], equals="firewall-java")
    assert "user" not in event
