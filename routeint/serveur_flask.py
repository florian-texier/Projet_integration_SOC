from json_responses import json_response, json_data, json_error
from flask import Flask, request
from flask_cors import CORS
import requests

app = Flask(__name__)
app.debug = True


@app.route('/')
def hello():
    return json_response('Hello World, from router api !')

#id inscription beacon
@app.route('/beaconInscript', methods=['GET'])
def beaconInscript():
    print("beaconInscript")
    r = requests.get('https://couchdb.mignolet.fr/beacondb/_design/type/_view/join?reduce=false')
    return json_response(r.json(),r.status_code)

#inscription team
@app.route('/inscript', methods=['POST'])
def inscript():
    #android id
    print("inscription team")
    id = request.get_json(force=True)
    jsonData = id["device_id"]
    r = requests.put('https://admin:adminsoc@couchdb.mignolet.fr/teambd/"002"')
    return json_response(r.json(), r.status_code)

#id beacon send picture
@app.route('/beaconSendPicture', methods=['GET'])
def beaconSendPicture():
    print("beaconInscript")
    r = requests.get('https://couchdb.mignolet.fr/beacondb/_design/type/_view/depot?reduce=false')
    return json_response(r.json(), r.status_code)

#liste objet search
@app.route('/newListe' , methods=['GET'])
def newsListe():
    print("newsListe")
    r = requests.get('https://couchdb.mignolet.fr/objetdb/_design/_all_dbs/_view/random?limit=10&reduce=false')
    return json_response(r.json(), r.status_code)

#liste objet search
@app.route('/listObjet' , methods=['GET'])
def ListObjet():
    print("item liste")
    r = requests.get('')
    return json_response(r.json(), r.status_code)

#inscrption instance scale
@app.route('/inscriptionInstance')
def inscrptionInsrance():
    return 'Hello World!'

#link instance for team
@app.route('/linkInstance')
def linkInstance():
    return 'Hello World!'


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

