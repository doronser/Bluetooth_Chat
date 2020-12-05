# Bluetooth_Chat
A simple android app developed for a university course. The bluetooth communication is between the mobile phone (client) and a laptop (server).

#Usage
0. Pair your android phone/tablet to your laptop

1. build the app using android studio and install on your phone/tablet via USB.

2. run the bluetooth server script (see pybluez library for more information). 

3. open the app and connect to your laptop (must be previously paired).

4. Chat away :)

# Functionallity
The chat app allows the cleint to send and receive messages as they please.

The server sends a welcome message to a client once a connection is established.

Since this is a simple app, the server simply send back any message received to the sender in all caps and with a prefix of "server received: ".
