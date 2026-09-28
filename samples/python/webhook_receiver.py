"""
AutoID CloudEvents Webhook Ingestion Service (Python / Flask)
Receives tag read and direction events from AutoID Edge Gateway.
"""

from flask import Flask, request, jsonify
import hmac
import hashlib
import json

app = Flask(__name__)
WEBHOOK_SECRET = "sample-shared-secret"

@app.route("/api/webhook/autoid", methods=["POST"])
def autoid_webhook():
    signature = request.headers.get("X-AutoID-Signature")
    raw_data = request.get_data()

    # Verify HMAC signature if secret is provided
    if WEBHOOK_SECRET and signature:
        expected = "sha256=" + hmac.new(
            WEBHOOK_SECRET.encode("utf-8"),
            raw_data,
            hashlib.sha256
        ).hexdigest()

        if not hmac.compare_digest(signature, expected):
            return jsonify({"error": "Unauthorized / Invalid Signature"}), 401

    event = request.get_json(force=True)
    event_type = event.get("type", "unknown")
    event_data = event.get("data", {})

    print(f"\n[CloudEvent] Type: {event_type} | ID: {event.get('id')}")
    if event_type == "com.beetech.autoid.tag.read":
        print(f"  -> Tag: EPC={event_data.get('epc')} RSSI={event_data.get('rssi')} Ant={event_data.get('antenna')}")
    elif event_type == "com.beetech.autoid.portal.transit":
        print(f"  -> ⭐ PORTAL TRANSIT: EPC={event_data.get('epc')} Direction={event_data.get('direction')} Speed={event_data.get('velocityMps')} m/s")

    return jsonify({"status": "SUCCESS", "received": True}), 200

if __name__ == "__main__":
    print("[Webhook Receiver] Listening on http://0.0.0.0:5000/api/webhook/autoid")
    app.run(host="0.0.0.0", port=5000)
