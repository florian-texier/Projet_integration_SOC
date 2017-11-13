from flask import Flask
from flask import request
from time import sleep
import datetime
from sense_hat import SenseHat

#init SenseHAT
sense = SenseHat()
sense.low_light = True
sense.set_rotation(180)
sense.clear(0,0,0)

#color led 
GREEN = [94,255,51]
RED = [240,24,14]
BLUE = [14,51,240]
O = [0, 0, 0] # white


valider = [
O,O,O,O,O,O,O,O,
O,O,O,O,O,O,O,O,
O,O,O,O,O,O,O,O,
O,O,O,O,O,O,O,BLUE,
O,O,O,O,O,O,BLUE,O,
O,BLUE,O,O,O,BLUE,O,O,
O,O,BLUE,O,BLUE,O,O,O,
O,O,O,BLUE,O,O,O,O
]

fail = [
RED,O,O,O,O,O,O,RED,
O,RED,O,O,O,O,RED,O,
O,O,RED,O,O,RED,O,O,
O,O,O,RED,RED,O,O,O,
O,O,O,RED,RED,O,O,O,
O,O,RED,O,O,RED,O,O,
O,RED,O,O,O,O,RED,O,
RED,O,O,O,O,O,O,RED
]

transfer = [
O,O,BLUE,O,O,O,O,O,
O,BLUE,O,BLUE,O,O,O,O,
BLUE,O,O,BLUE,O,O,O,O,
O,O,BLUE,O,O,BLUE,O,O,
O,O,BLUE,O,O,BLUE,O,O,
O,O,O,O,BLUE,O,O,BLUE,
O,O,O,O,BLUE,O,BLUE,O,
O,O,O,O,O,BLUE,O,O
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
