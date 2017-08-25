package dis_tutorial_multicast_example;

import java.io.*;
import java.net.*;

/**
 * A simple example of sending to multicast. The receving section of code can
 * receive the message, decode it, and print the contents.<p>
 * 
 * The message is intended to be DIS, but for simplicity reasons this
 * just encodes the DIS header without the full support.<p>
 * 
 * @author DMcG
 */
public class SendingThread extends Thread
{
    /** Passed to constructor, the socket to use for sending */
    private MulticastSocket multicastSocket = null;
    
    /** The multicast address to send to */
    private InetAddress multicastAddress = null;
    
    /** A unique identifier for the sender, placed into the data sent */
    private int participantID = 0; 
    
    /** Some state information to be transmitted. This is made-up data content,
     * intended to explain networking, not DIS. 
     * @param multicastSocket Socket used to send data
     * @param mutlicastAddress mcast address to send to
     * @param participantID A unique identifier for the sending process. At least most likely unique.
     */
    public SendingThread( MulticastSocket multicastSocket, InetAddress mutlicastAddress, int participantID )
    {
        this.multicastSocket = multicastSocket;
        this.participantID = participantID;
        this.multicastAddress = multicastAddress;
    }
    
    /** the run() method is called when the owning thread starts. In this case
     * we generate a message containing, in binary form, two integers: an integer
     * that is unique (most likely) for the sending process, and a counter that
     * contains how many messages have been sent by this process. The run()
     * method sends out messages. This is the sort of place that can be used
     * to send DIS messages, which is left out in the interests of showing what
     * the networking looks like.
     */
    public void run()
    {
       int count = 0; // How many messages this thread has send
       
        while( true )
        {
            try
            {
                
                count++;
                
                byte data[] = new byte[Multicast_Example.MAX_PACKET_SIZE]; // size of data package (as picked by us

                // Put together a message to send. From a Java IO standpoint, using a
                // standard IO library is not that bad.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(baos);
                out.writeInt(participantID);
                out.writeInt(count);
                out.flush();
                      
                // We wrote some data; we want to retrieve that as a binary array. 
                byte[] writeData = baos.toByteArray();
                
                // Create a networking packet, which uses as a destination address
                // the multicast address, and with a payload of what we just created
                // above. We use the port number of the socket we're using.
                DatagramPacket packet = new DatagramPacket(writeData, writeData.length, InetAddress.getByName(Multicast_Example.MULTICAST_DIS_TRAFFIC), Multicast_Example.MULTICAST_PORT);
                
                // We've got a filled out datagram packet. Send it out the socket
                multicastSocket.send(packet);
                
                // Frame a "sent message" output. This can result in a thread
                // problem if the receiver thread prints out a message too
                // fast.
                System.out.println("----");
                System.out.println("sent multicast data message number " + count + 
                           " from participant ID " + participantID);
                System.out.println("----");
                
                // We want to send data at a reasonable rate, rather than as
                // fast as the loop can cycle. That would cause an overload on
                // the network. Instead we simply wait for a while by sleeping,
                // then repeat what we just did.
                Thread.sleep(Multicast_Example.MESSAGE_SEND_RATE);
                
            }
            catch(Exception e)
            {
                // If we went wrong with an exception above, we don't want a result
                // of sending as quickly as possible by another route. If there's
                // an exception, wait until sending again, which will also probably
                // result in an error.
                System.out.println(e);
                System.out.println("Invaid data in SendingThread");
                try{
                    Thread.sleep(Multicast_Example.MESSAGE_SEND_RATE);
                }
                catch(Exception er)
                {
                    
                }
                
                
            }
            
            

            
            
        }
        
    }

}
