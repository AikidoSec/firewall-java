from .make_post_request import  make_post_request

def test_safe_vs_unsafe_payloads(payloads, urls, route=""):
    print("Safe req to : (1) " + urls["enabled"])
    make_post_request(urls["enabled"]  + route, payloads["safe"], status_code=200)
    print("Safe req to : (0) " + urls["disabled"])
    make_post_request(urls["disabled"] + route, payloads["safe"], status_code=200)
    print("Unsafe req to : (1) " + urls["enabled"])
    make_post_request(urls["enabled"] + route, payloads["unsafe"], status_code=500)
    print("Unsafe req to : (0) " + urls["disabled"])
    make_post_request(urls["disabled"] + route, payloads["unsafe"], status_code=200)
