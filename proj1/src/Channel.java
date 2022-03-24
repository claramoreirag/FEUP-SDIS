
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

// Class that represents a channel
public class Channel implements Runnable {
  private String inetAddress;
  private int port;
  private InetAddress address;

  // Class constructor
  public Channel(String inetAddress, int port){

    try{
      this.inetAddress = inetAddress;
      this.port = port;
      address = InetAddress.getByName(inetAddress);
    }
    catch(UnknownHostException e){
      e.printStackTrace();
    }
  }

  // sends message
  public void send(byte[] message){

    try( DatagramSocket socket = new DatagramSocket()){
      DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
      socket.send(packet);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  // runs thread do receive messages
  @Override
  public void run() {
    byte[] buffer = new byte[64200];

    try{
      // open multicast socket
      MulticastSocket socket = new MulticastSocket(port);

      socket.joinGroup(address);

      while(true){

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        byte[] msg = Arrays.copyOf(buffer, packet.getLength());
        
        //executes thread that interprets messages
        Peer.exec.execute(new InterpretMsgThread(msg));

        buffer = new byte[64200]; 

      }

    } catch(IOException e){
      e.printStackTrace();
    }


  }
}
