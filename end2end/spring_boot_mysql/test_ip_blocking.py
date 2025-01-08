import requests
from utils.assert_equals import assert_eq

def test_ip_blocking(url):
    # Allowed IP :
    res = requests.get(url, headers={
        'X-Forwarded-For': "192.168.1.1"
    })
    assert_eq(res.status_code, equals=200)

    # Blocked IP :
    res = requests.get(url, headers={
        'X-Forwarded-For': "1.2.3.4"
    })
    assert_eq(res.status_code, equals=403)
    assert_eq(res.text, equals="")