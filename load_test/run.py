import requests
import json
import os

API_URL = "http://localhost:8080/api/v1/employees/create"
HEADERS = {"Content-Type": "application/json"}

# ---- Load employees from employees.json ----
file_path = os.path.join(os.path.dirname(__file__), "employees.json")

try:
    with open(file_path, "r") as f:
        employees = json.load(f)
except Exception as e:
    print(f"ERROR: Unable to read employees.json -> {e}")
    exit(1)

# --------------------------------------------

print(f"Starting bulk upload of {len(employees)} employees...\n")

for idx, emp in enumerate(employees, start=1):
    try:
        response = requests.post(API_URL, headers=HEADERS, json=emp)

        if response.status_code == 200:
            print(f"[{idx}] SUCCESS: {emp.get('email')}")
        else:
            print(f"[{idx}] FAILED: {emp.get('email')} -> {response.status_code} {response.text}")

    except Exception as e:
        print(f"[{idx}] ERROR sending {emp.get('email')} -> {e}")

print("\nBulk upload complete.")
