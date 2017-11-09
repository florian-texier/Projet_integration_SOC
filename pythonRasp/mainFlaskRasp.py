from flask import Flask
from flask import request
from time import sleep
import datetime
from sense_hat import SenseHat

X = [255, 0, 0] #rouge
O = [0, 0, 0] #blanc
Z = [255,255,0] #jaune
W = [0,0,255] #bleu

#init SenseHAT
sense = SenseHat()
sense.low_light = True
sense.set_rotation(180)
sense.clear(0,0,0)

#color led 
GREEN = (94,255,51)
RED = (240,24,14)
BLUE = (14,51,240)


valider = [
O,O,O,O,O,O,O,O,
O,O,O,O,O,O,O,O,
O,O,O,O,O,O,O,O,
0,O,O,O,0,O,O,BLUE,
O,0,O,0,O,0,BLUE,0,
O,BLUE,0,O,O,BLUE,0,O,
O,O,BLUE,O,BLUE,0,O,O,
O,O,O,BLUE,O,O,O,O
]

fail = [
RED,O,O,0,0,O,O,RED,
O,RED,O,0,0,O,RED,O,
O,O,RED,0,0,RED,O,O,
O,0,0,RED,RED,0,0,O,
O,0,0,RED,RED,0,0,O,
O,0,RED,0,0,RED,0,O,
O,RED,0,0,0,0,RED,O,
RED,O,0,0,0,0,O,RED
]

transfer = [
O,O,BLUE,0,0,O,O,O,
O,BLUE,O,BLUE,0,O,O,O,
BLUE,O,0,BLUE,0,0,O,O,
O,0,BLUE,0,0,BLUE,0,O,
O,0,BLUE,0,0,BLUE,0,O,
O,0,0,0,BLUE,0,0,BLUE,
O,0,0,0,BLUE,0,BLUE,O,
O,O,0,0,0,BLUE,O,O
] 

app = Flask(__name__)

#action transfer picture instance equipe 
@app.route("/transfer", methods=['GET'])
def transfer():
        sense.set_pixels(transfer)
        return '{"msg": "transfer en cours","code": true,"error": null}'

#acnti
@app.route("/yes", methods=['GET'])
def yesPicture():
        sleep(1)
        sense.clear(0,0,0)
        sleep(1)
        sense.set_pixels(valider)
        sleep(8)
        sense.clear(0,0,0)
        sleep(1)
        return '{"msg": "image valide","code": true,"error": null}'

@app.route("/fail", methods=['GET'])
def failPicture():
        sleep(1)
        sense.clear(0,0,0)
        sleep(1)
        sense.set_pixels(fail)
        sleep(5)
        sense.clear(0,0,0)
        sleep(1)
        return '{"msg": "image non valide","code": false,"error": null}'



if __name__ == "__main__":
        app.debug = True
        app.run(host='0.0.0.0')
