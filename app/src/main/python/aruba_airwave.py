# This piece of code runs locally as a standalone executable (that has nothing to do with
# the rest of the codebase) in a machine that will have access to the AirWave API.
# PyInstaller can be used to make Python scripts executable.

from http.cookiejar import LWPCookieJar
import requests
import urllib3
from datetime import datetime
import xml.etree.ElementTree as ET
import json

urllib3.disable_warnings()

DATASET_FLOOR_COLUMN = "Floor"
DATASET_COUNT_COLUMN = "Count"
TIMESTAMP_COLUMN = "_start_date"

folder_ids = {
    "2": "58", # Main floor
    "3": "66",
    "4": "67",
    "5": "61",
    "6": "62",
    "7": "63",
    "8": "65",
    "9": "64",
    "10": "59",
    "11": "68",
    "20": "104" # Makerspace
}

def get_xml_data() -> dict:
    cookie_file = "/tmp/cookies"
    jar = LWPCookieJar(cookie_file)

    try:
        jar.load()
    except:
        pass

    req = requests.Session()
    req.cookies = jar

    headers = {
        "Content-Type": "application/x-www-form-urlencoded"
    }

    data = {
        "destination": "/",
        "credential_0": "username",
        "credential_1": "password"
    }

    try:
        response = req.post("https://cits-slairwave.campus.brocku.local/LOGIN", headers=headers, data=data, verify=False, timeout=5)
    except:
        return {}

    token = response.headers['X-BISCOTTI']

    headers = {
        "Content-Type": "application/xml",
        "X-BISCOTTI": token
    }

    try:
        xmlData = req.get("https://cits-slairwave.campus.brocku.local/folder_list.xml", headers=headers, verify=False).text

        with open("data.txt", "w") as f:
            f.write(xmlData)
    except:
        return {}

    root = ET.fromstring(xmlData)
    built_dict = {}

    for child in root:
        if child.attrib["id"] in folder_ids.values():
            try:
                built_dict[child.attrib["id"]] = {
                    "count": int(child.find("client_count").text) if child.find("client_count") != None else 0,
                    "name": child.find("name").text
                }
                
            except:
                print(child.attrib["id"])

    return built_dict

if __name__ == "__main__":
    xml_data = get_xml_data()
        
    floor_data_patron = []
    time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    div_factor = 2.0 if (datetime.now().hour >= 11) else 1.7

    for floor, floor_id in folder_ids.items():
        dataPatron = {
            DATASET_FLOOR_COLUMN: floor,
            DATASET_COUNT_COLUMN: xml_data[floor_id]['count'] // div_factor,
            TIMESTAMP_COLUMN: time
        }
        
        floor_data_patron.append(dataPatron)
    
    # Pushing the data to LibUtils backend directly, instead of relying on LibInsight, since LibInsight often responds with stale data
    response = requests.post("http://rtod.library.brocku.ca:32777/busylib", data= {"jsonString": json.dumps(floor_data_patron)})
