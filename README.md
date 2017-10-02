# DIS_Tutorial_Multicast_Example
Simple Java program that shows sending and receiving DIS binary data with a multicast project. 

Also provided as a example repository for wiki servers.

The application demonstrates what's involved in sending UDP multicast messages. In this
case one thread sends PDUs, while another threat in the same application receives them. 
It uses the open-dis open source java project for DIS Protocol Data Units (PDUs). 

On the sending side the open-dis project converts the java object corresponding to
an Entity State PDU in DIS into the approved IEEE 1278.1 DIS binary format. It then
sends that byte array to the network.

If you have the utility WireShark (http://www.wireshark.org) you can detect the DIS
PDUs sent on the network. Wireshark includes a DIS binary interpreter, and you can see
the entire content of the PDU.

Be aware that the Cisco AnyConnect remote access application causes endless problems
with network identificatons. If on, turn it off, and restart.

The receiving thread receives the message and decodes it in two ways. Every PDU starts
with the same three fields. We can decode those fields ourselves, or use open-DIS to decode
the entire PDU. We first decode the fiels manually:

~~~
  ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
  DataInputStream dis = new DataInputStream(bais);
  int disProtocolVersion = dis.readByte();
  int exerciseIdentifier = dis.readByte();
  int pduType = dis.readByte();
~~~

The puyrpose of the fields are discussed later. The "exerciseIdentifier" is between 0-255
and is different, due to a random number selection, every time it is run.
