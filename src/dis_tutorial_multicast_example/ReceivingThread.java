
package dis_tutorial_multicast_example;

import java.io.*;
import java.net.*;
import edu.nps.moves.disutil.PduFactory;
import edu.nps.moves.dis.*;

/**
 * A simple example of reading data from a multicast socket. <p>
 * 
 * open-dis has organized conversions of binary DIS data on the
 * network converted into a Java object. You can also manually
 * decode the binary using standard programming techniques.
 * For reasons of instruction I do a bit of both here.
 * 
 * @author DMcG
 */
public class ReceivingThread extends Thread
{
    /** Socket to use for reading network messages. Also used by the sender. */
    private MulticastSocket socket = null;
    
    /** Multicast address to send to, passed to the constructor */
    private InetAddress multicastAddress;
    
    /** Basic setup
     * 
     * @param socket UDP/multicast socket to send to
     * @param multicastAddress Multicast address to read from
     * ID of the message we received from, provided it was sent from another program
     */
    public ReceivingThread(MulticastSocket socket, InetAddress multicastAddress )
    {
        this.socket = socket;
        this.multicastAddress = multicastAddress;
    }
    
    /**
     * Called when this was started as a thread. Loops, reading messages from the
     * network, and decoding them.
     */
    public void run()
    {
        // A pduFactory decodes binary format DIS messages and
        // turns them into Java objects.
        PduFactory pduFactory = new PduFactory();
        
        while(true)
        {
            try
            {
                // Read from the UDP socket. Decode the binary data. The format
                // of the binary data is something we must know beforehand; look
                // in the SendingThread to confirm this. Construct the packet to
                // be able to hold the expected amount of data send. 
                byte[] payload = new byte[Multicast_Example.MAX_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(payload, payload.length);

                // Blocks until we receive a message packet on the socket
                socket.receive(packet);

                // Pretty normal way to decode the data in a standard Java IO library
                // way
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                DataInputStream dis = new DataInputStream(bais);
                int disProtocolVersion = dis.readByte();
                int exerciseIdentifier = dis.readByte();
                int pduType = dis.readByte();
                
                System.out.println("----");
                System.out.println("local programming decoding. disVersion:" + disProtocolVersion + " exerciseIdentifier:" + exerciseIdentifier + " pduType:" + pduType);
                Pdu aPdu = pduFactory.createPdu(packet.getData());
                
                disProtocolVersion = aPdu.getProtocolVersion();
                exerciseIdentifier = aPdu.getExerciseID();
                pduType = aPdu.getPduType();
                System.out.println("open-dis decoding. disVersion:" + disProtocolVersion + " exerciseIdentifier:" + exerciseIdentifier + " pduType:" + pduType);
                
                
                System.out.println("----");
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
        
    }

}
