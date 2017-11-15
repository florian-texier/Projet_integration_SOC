#-*- encoding:utf-8 -*-
from time import sleep
import datetime
from sense_hat import SenseHat
import os, urlparse
import paho.mqtt.client as mqtt
import json


#init SenseHAT
sense = SenseHat()
sense.low_light = True
sense.set_rotation(180)
sense.clear(0,0,0)

#color led 
GREEN = [94,255,51]
RED = [255,0,0]
BLUE = [14,51,240]
O = [0, 0, 0] # white
YL = [200,0,255]

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

t = [
O,O,BLUE,O,O,O,O,O,
O,BLUE,BLUE,BLUE,O,O,O,O,
BLUE,O,BLUE,O,BLUE,YL,O,O,
O,O,BLUE,O,O,YL,O,O,
O,O,BLUE,O,O,YL,O,O,
O,O,BLUE,YL,O,YL,O,YL,
O,O,O,O,YL,YL,YL,O,
O,O,O,O,O,YL,O,O
]

# Define event callbacks
def on_connect(client, userdata, flags, rc):
    print("rc: " + str(rc))

def on_message(client, obj, msg):
    print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))

def on_publish(client, obj, mid):
    print("mid: " + str(mid))

def on_subscribe(client, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))
    if granted_qos == "":
        print("not info")
    else:
        status = json.loads(grandted_qos)
        print(status)
    

def on_log(client, obj, level, string):
    print(string)
    
mqttc = mqtt.Client()
mqttc.on_message = on_message
mqttc.on_connect = on_connect
mqttc.on_subscribe = on_subscribe
# Uncomment to enable debug messages
#mqttc.on_log = on_log

# Parse CLOUDMQTT_URL (or fallback to localhost)
url_str = os.environ.get('CLOUDMQTT_URL', "mqtt://hrrgcrqx:af3wqGskmMfY@m20.cloudmqtt.com:12771")
url = urlparse.urlparse(url_str)

# Connect
mqttc.username_pw_set(url.username, url.password)
mqttc.connect(url.hostname, url.port)
mqttc.subscribe("picture/test", 0)
# Publish a message
rc = 0
while rc == 0:
    rc = mqttc.loop()
    print("rc: " + str(rc))