import gzip

from flask import Flask, request, jsonify, Response
import sys
import os
import json
import time

app = Flask(__name__)

responses = {
    "config": {
        "receivedAnyStats": False,
        "success": True,
        "endpoints": [
            {
                "route": "/test_ratelimiting_1",
                "method": "*",
                "forceProtectionOff": False,
                "rateLimiting": {
                    "enabled": True,
                    "maxRequests": 2,
                    "windowSizeInMS": 1000 * 5,
                },
                "graphql": False,
            },
            {
                "route": "/api/pets/create",
                "method": "POST",
                "forceProtectionOff": False,
                "rateLimiting": {
                    "enabled": False,
                },
                "graphql": False,
            },
            {
                "route": "/api/*",
                "method": "*",
                "forceProtectionOff": False,
                "rateLimiting": {
                    "enabled": False,
                },
                "graphql": False,
            }
        ],
        "blockedUserIds": ["12345"],
        "block": True,
    },
    "lists": {
        "success": True,
        "blockedIPAddresses": [
            {
                "key": "geo-1",
                "source": "geoip",
                "description": "geo restrictions",
                "ips": ["1.2.3.4"]
            },
        ],
        "monitoredIPAddresses": [
            {
                "key": "geo-2",
                "source": "geoip",
                "description": "should not be blocked",
                "ips": ["5.6.7.8"]
            }
        ],
        "blockedUserAgents": "AI2Bot|Bytespider",
        "monitoredUserAgents": "ClaudeUser",
        "userAgentDetails": [
            {
                "key": "ai-agents",
                "pattern": "AI2Bot"
            },
            {
                "key": "crawlers",
                "pattern": "Bytespider"
            },
            {
                "key": "crawlers-monitor",
                "pattern": "ClaudeUser"
            }
        ],
    },
    "configUpdatedAt": 0,
}

events = []

# Realtime
@app.route('/realtime/config', methods=["GET"])
def get_realtime_config():
    return jsonify({"configUpdatedAt": responses["configUpdatedAt"]})

@app.route('/api/runtime/config', methods=['GET'])
def get_runtime_config():
    return jsonify(responses["config"])

@app.route('/api/runtime/firewall/lists', methods=['GET'])
def get_fw_lists():
    accept_encoding = request.headers.get('Accept-Encoding', '').lower()
    if 'gzip' not in accept_encoding:
        return jsonify({
            "success": False,
            "error": "Accept-Encoding header must include 'gzip' for firewall lists endpoint"
        }), 400

    json_data = json.dumps(responses["lists"])
    compressed_data = gzip.compress(json_data.encode('utf-8'))

    response = Response(compressed_data, content_type='application/json')
    response.headers['Content-Encoding'] = 'gzip'
    return response

@app.route('/api/runtime/events', methods=['POST'])
def post_events():
    print("Got event: ", request.get_json())
    if request.get_json():
        events.append(request.get_json())
    return jsonify(responses["config"])

@app.route('/delayed/<int:delay>/api/runtime/config')
def delayed_route(delay):
    time.sleep(delay)
    return jsonify(responses["config"])

@app.route('/mock/config', methods=['POST'])
def mock_set_config():
    configUpdatedAt = int(time.time())
    responses["config"] = request.get_json()
    responses["config"]["configUpdatedAt"] = configUpdatedAt
    responses["configUpdatedAt"] = configUpdatedAt
    return jsonify({})


@app.route('/mock/events', methods=['GET'])
def mock_get_events():
    return jsonify(events)

@app.route('/mock/reset', methods=['GET'])
def mock_reset():
    global events
    events = [] # Reset events
    return jsonify({})

@app.route('/mock/set_protection', methods=['POST'])
def mock_set_protection():
    req = request.get_json()
    global responses
    responses["config"]["endpoints"][1]["forceProtectionOff"] = bool(req["api_pets_create"])
    responses["config"]["endpoints"][2]["forceProtectionOff"] = bool(req["api"])
    responses["config"]["configUpdatedAt"] = int(time.time()*1000)
    responses["configUpdatedAt"] = int(time.time()*1000)

    return jsonify({})

if __name__ == '__main__':
    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print("Usage: python mock_server.py <port> [config_file]")
        sys.exit(1)

    port = int(sys.argv[1])

    if len(sys.argv) == 3:
        config_file = sys.argv[2]
        if os.path.exists(config_file):
            try:
                with open(config_file, 'r') as file:
                    configUpdatedAt = int(time.time())
                    responses["config"] = json.load(file)
                    responses["config"]["configUpdatedAt"] = configUpdatedAt
                    responses["configUpdatedAt"] = { "serviceId": 1, "configUpdatedAt": configUpdatedAt }
                    print(f"Loaded runtime config from {config_file}")
            except json.JSONDecodeError:
                print(f"Error: Could not decode JSON from {config_file}")
                sys.exit(1)
        else:
            print(f"Error: File {config_file} not found")
            sys.exit(1)

    app.run(host='0.0.0.0', port=port)
