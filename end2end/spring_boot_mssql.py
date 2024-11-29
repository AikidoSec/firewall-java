import requests
import json

# Define the data to be sent in the POST request
safe_payload = {
    "name": "Bobby"
}
unsafe_payload = {
    "name": "Malicious Pet', 'Gru from the Minions'); -- "
}

# Define the URLs for both applications
url_with_zen = "http://localhost:8084/api/pets/create"
url_without_zen = "http://localhost:8085/api/pets/create"

# Function to make a POST request
def make_post_request(url, data, status_code):
    response = requests.post(url, json=data)

    # Assert that the status code is 200
    assert response.status_code == status_code, f"Expected status code {status_code} but got {response.status_code}"

    # If you want to check the response content, you can do so here
    print(f"Success: {response.json()}")

print("Making safe request to app with Zen...")
make_post_request(url_with_zen, safe_payload, status_code=200)

print("Making safe request to app without Zen...")
make_post_request(url_without_zen, safe_payload, status_code=200)

print("Making unsafe request to app with Zen...")
make_post_request(url_with_zen, unsafe_payload, status_code=500)

print("Making unsafe request to app without Zen...")
make_post_request(url_without_zen, unsafe_payload, status_code=200)
