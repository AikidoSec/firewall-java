import requests

# Function to make a POST request
def make_post_request(url, data, status_code):
    response = requests.post(url, json=data)

    # Assert that the status code is 200
    assert response.status_code == status_code, f"Expected status code {status_code} but got {response.status_code}"

    # If you want to check the response content, you can do so here
    print(f"Success: {response.json()}")
