import java.io.UnsupportedEncodingException;

//Class that sends the awake message
public class AwakeMessage extends Message {


  //Class constructor
  public AwakeMessage(String version, int senderId){
    super(version, senderId, " ");
  }

  //builds awake message
  @Override
  protected byte[] buildHeader() throws UnsupportedEncodingException{
    String header = version + " AWAKE " + senderId + "\r\n\r\n";
    byte[] headerBytes = header.getBytes("US-ASCII");
    return headerBytes;
  }

  //sends awake message
  @Override
  protected void send(){
    try{
      byte[] message = buildHeader();
      SendMessageThread sendThread = new SendMessageThread(message, "control");
      System.out.println("I'm awake!\n");
      Peer.exec.execute(sendThread);

    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  
}
