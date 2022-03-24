import java.io.UnsupportedEncodingException;

// Class that represents GETCHUNK message and sends it
public class RestoreMessage extends Message{

  private int chunkNo;

  // Class constructor
  public RestoreMessage(String version, int senderId, String fileId, int chunkNo){
    super(version, senderId, fileId);
    this.chunkNo = chunkNo;
  }

  // builds GETCHUNK header
  @Override
  protected byte[] buildHeader() throws UnsupportedEncodingException{
    String header = version + " GETCHUNK " + senderId + " " + hashedfileId + " " + chunkNo + "\r\n\r\n";
    
    byte headerBytes[] = header.getBytes("US-ASCII");
    return headerBytes;
  }

  // sends GETCHUNK message
  @Override
  protected void send(){
    try{
      byte[] message = buildHeader();
      
      SendMessageThread sendThread = new SendMessageThread(message, "restore");
      Peer.exec.execute(sendThread);
      
    }
    catch(UnsupportedEncodingException e){
      e.printStackTrace();
    }
  }
  
}
