package dis_tutorial_multicast_example;

import java.io.*;
import java.net.*;

// DIS libraries, an open source implementation of DIS
import edu.nps.moves.dis.*;

/**
 * A simple example of sending to multicast. The receiving section of code can
 * receive the message, decode it, and print the contents.<p>
 * 
 * The message is in format DIS. This is a little complex, and as a lot
 * of jar files in the lib directory.<p>
 * 
 * @author DMcG
 */
public class SendingThread extends Thread
{
    /** Passed to constructor, the socket to use for sending */
    private MulticastSocket multicastSocket = null;
    
    /** The multicast address to send to */
    private InetAddress multicastAddress = null;
     
    
    /** Some state information to be transmitted. This is made-up data content,
     * intended to explain networking, not DIS. 
     * @param multicastSocket Socket used to send data
     * @param mutlicastAddress mcast address to send to
     */
    public SendingThread( MulticastSocket multicastSocket, InetAddress multicastAddress)
    {
        this.multicastSocket = multicastSocket;
        this.multicastAddress = multicastAddress;
    }
    
    /** the run() method is called when the owning thread starts. In this case
     * we generate a message containing, in binary form, two integers: an integer
     * that is unique (most likely) for the sending process, and a counter that
     * contains how many messages have been sent by this process. The run()
     * method sends out messages. 
     */
    public void run()
    {
       int count = 0; // How many messages this thread has send
       
       // An entity state PDU. Uninitialized but we'll set a couple values
       EntityStatePdu espdu = new EntityStatePdu();
       
       // We do not set the time, which we should.
       // There are a lot of other fields that should be set.
       // What we will do is set some fields near the start of
       // the binary message, and decode it on the receiving side.
       // There's code in open-dis to do this, but I wanted to show
       // the process to readers. You can also examine the complete content
       // of the ESPDU with the help of Wireshark, https://www.wireshark.org/
       
       // A mostly random ID for the PDU
       EntityID id = espdu.getEntityID();
       id.setSite(1);
       id.setApplication(2);
       id.setApplication((int)( Math.random() * 30000) );
       
       // The protocol version, exerciseID and PDU Type fields are the
       // first three in the binary packet. All three are binary bytes
       // in big-endian format (that is, not used by intel.) Protocol
       // and PDUType are set by the constructor
       espdu.setExerciseID((byte)( Math.random() * 120));
       
        while( true )
        {
            try
            {
                count++;
                
                byte data[] = new byte[Multicast_Example.MAX_PACKET_SIZE]; // size of data package (as picked by us

                // Put together a message to send. From a Java IO standpoint, using a
                // standard IO library is not that bad.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                // Use open-dis to marshal the data from an object to a binary
                // array. This turns a Java object into an array of bytes
                // that have the field values.
                byte[] writeData = espdu.marshalWithDisRelativeTimestamp();
                
                
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
                System.out.println("sent multicast data message number " + count); 
      
                
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
