import java.io.UnsupportedEncodingException;

// Class that represents a DELETED message and sends it
public class DeletedMessage extends Message {

  // Class constructor
  public DeletedMessage(String version, int senderID, String fileID){
    super(version, senderID, fileID);
  }
  
  // Builds DELETED header
  @Override
  protected byte[] buildHeader() throws UnsupportedEncodingException{
    String header = version + " DELETED " + senderId + " " + hashedfileId + "\r\n\r\n";
    byte[] headerBytes = header.getBytes("US-ASCII");
    return headerBytes;
  }

  // Sends DELETED message
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
