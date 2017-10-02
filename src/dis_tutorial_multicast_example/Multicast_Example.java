
package dis_tutorial_multicast_example;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The application CiscoAnyconnect causes endless problems when
 * resolving multicast IP numbers. Turn it off. InetAddress.getByName("239.1.2.3"); will
 * generate a result that will fail when for example trying to send. It
 * will attempt to send out all interfaces, and anything interface related to Cisco
 * AnyConnect will cause endless problems. Make sure AnyConnet is turned off.<p>
 * 
 * Also, note that -Djava.net.preferIPv4Stack=true is set in VMOptions of "run"
 * properties in NetBeans. In the code below as well, because I'm that skeptical.
 * Basically it won't get an IPv4 interface, which cases problems. Google it.
 * 
 * @author DMcG
 */
public class Multicast_Example 
{
    
    /**
     * This is the maximum number of routers that a multicast
     * packet can cross. The default is 1, which limits
     * it to the local network only. The header of the
     * UDP sent has a TTL of 1, the router receives it, and
     * lowers it by one. THe resulting TTL has a value of zero, 
     * and the router will drop the UDP packet rather than forwarding
     * it to the next network. If you want it to be shared across
     * networks you should set the TTL higher.
     */
    public static final int MAX_ROUTER_HOPS = 5;
    
    /** This is how large the UDP/multicast packets can be. 
     * 1500 bytes is one popular value. Another is 8K. Sometimes
     * several PDUs are placed in one UDP packet, and the larger
     * UDP packet is needed for this. Ethernet will typically split
     * an 8K into several Ethernet frames, and if any of the Ethernet
     * frames are dropped, the entire UDP (and DIS) PDU will be
     * dropped.
     */
    public static final int MAX_PACKET_SIZE = 8 * 1024;
    
    /** The multicast group that is subscribed to. We can send and
     * receive to other hosts that are subscribed to this group. It's
     * also possible to subscribe to several multicast groups on the
     * same socket. This can be used to do things such as different
     * multicast groups for different entity types, or different regions.
     */
    public static final String MULTICAST_DIS_TRAFFIC = "239.1.2.3";
    
    /** What UDP port to listen and send on */
    public static final int MULTICAST_PORT = 3000;
    
    /** How often we send messages, in milliseconds */
    public static final int MESSAGE_SEND_RATE = 10000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        
        MulticastSocket socket;       // Socket on which to to send and receive
        InetAddress multicastAddress; // Non-text verson of multicast group
        
        try
        {
           // This is part of an irritating problem in OS X. The OS can
            // create sockets with IPv6 interfaces that cause problems. This
            // tries to limit everything to IPv4, along with a 
            // -Djava.net.preferIPv4Stac=true" entry in the VM run options
            // to launch the program. There also seems to be some sort of 
            // time delay involved. There's a print session to show whether
            // all the network interfaces support multicast; if not, you
            // get network failures. Running the printInterfaces() method shows
            // what interfaces support mcast; should be all.
           System.setProperty("java.net.preferIPv4Stack", "true");
           
            // Start configuring the socket
            socket = new MulticastSocket( MULTICAST_PORT );
            
            // This is how many routers the multicast packets can cross. With 
            // broadcast, this is zero. Multicast can scale that from local network
            // to campus network to regional network to world network with a big
            // enough number and enough network configuration.
            socket.setTimeToLive( MAX_ROUTER_HOPS );
            
            // Show the network interfaces to the user
            Multicast_Example.printInterfaces();
            
            // Convert the multicast address we will use to send and receive
            // messages from a string to a Java object to be used when interacting
            // with the socket
            multicastAddress = InetAddress.getByName(MULTICAST_DIS_TRAFFIC);
            
            // Tell the multicast socket we created we are interested in traffic
            // to this group. We can, if liked, join several multicast groups on
            // this socket. This will cause messages sent to the mcast addresses to be
            // read by the socket.
            socket.joinGroup(multicastAddress);
            System.out.println("joined multicast support " + multicastAddress);
                       
            // Two threads. One periodically sends messages. The other listens for messages.
            // This is full duplex. 
            SendingThread sendingThread = new SendingThread(socket, multicastAddress);
            ReceivingThread receivingThread = new ReceivingThread(socket, multicastAddress);
            
            receivingThread.start();
            sendingThread.start();
            
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.out.println("Invaid data");
        }
        
    }
    
    /** Simple info provided to the user
     * 
     */
   private static void printInterfaces()
   {
       try
       {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            displayInterfaceInformation(netint);
       }
       catch(Exception e)
       {
           System.out.println("problem in printInterfaces");
       }
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        System.out.println("Supports multicast? " + netint.supportsMulticast());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
     }
   }
  
