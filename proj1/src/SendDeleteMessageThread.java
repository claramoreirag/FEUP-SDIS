import java.util.ArrayList;

// Thread to send DELETE message
public class SendDeleteMessageThread implements Runnable {
  
  private ArrayList<String> filesToDelete;
  private String version;
  private int senderId;

  // Thread constructor
  public SendDeleteMessageThread(ArrayList<String> filesToDelete, String version, int senderId){
    this.filesToDelete = filesToDelete;
    this.version = version;
    this.senderId = senderId;
  }

  // sends delete message
  public void run(){
    for(String file : filesToDelete){
      DeleteMessage message = new DeleteMessage(version, senderId, file);
      message.send();
    }
  }
}
