import java.io.UnsupportedEncodingException;

// Class that represents DELETE message and sends it
public class DeleteMessage extends Message {
  
  // Class constructor
  public DeleteMessage(String version, int senderID, String fileID){
    super(version, senderID, fileID);
  }

  // Builds DELETE header
  @Override
  protected byte[] buildHeader() throws UnsupportedEncodingException{
    String header = version + " DELETE " + senderId + " " + hashedfileId + "\r\n\r\n";
    byte[] headerBytes = header.getBytes("US-ASCII");
    return headerBytes;
  }

  // Sends DELETE message
  @Override
  protected void send(){
    try{
      byte[] message = buildHeader();
      SendMessageThread sendThread = new SendMessageThread(message, "control");
      
      Peer.exec.execute(sendThread);

    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

}
