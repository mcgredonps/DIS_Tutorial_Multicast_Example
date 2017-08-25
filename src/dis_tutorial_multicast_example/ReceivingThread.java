
package dis_tutorial_multicast_example;

import java.io.*;
import java.net.*;

/**
 * A simple example of reading data from a multicast socket. <p>
 * 
 * Normally one would expect to decode a DIS message (or messages) here,
 * but instead we simply decode simple data. The objective is to show
 * networking, not DIS.<p>
 * 
 * @author DMcG
 */
public class ReceivingThread extends Thread
{
    /** Socket to use for reading network messages. Also used by the sender. */
    private MulticastSocket socket = null;
    
    /** Each copy of the sender/recevier has a unique ID. Other processes should
     * have different IDs.
     */
    private int participantID = 0;
    
    /** Multicast address to send to, passed to the constructor */
    private InetAddress multicastAddress;
    
    /** Basic setup
     * 
     * @param socket UDP/multicast socket to send to
     * @param multicastAddress Multicast address to read from
     * @param participantID The unique ID of this program. It should differ from the 
     * ID of the message we received from, provided it was sent from another program
     */
    public ReceivingThread(MulticastSocket socket, InetAddress multicastAddress , int participantID)
    {
        this.socket = socket;
        this.multicastAddress = multicastAddress;
        this.participantID = participantID;
    }
    
    /**
     * Called when this was started as a thread. Loops, reading messages from the
     * network, and decoding them.
     */
    public void run()
    {
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
                int senderID = dis.readInt();
                int messageCount = dis.readInt();
                
                System.out.println("----");
                System.out.println("Receiver ID " + this.participantID + " got message from sender ID " + senderID + " with sender message sequence number "+ messageCount);
                System.out.println("----");
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
        
    }

}
