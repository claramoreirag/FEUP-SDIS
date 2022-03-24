import java.util.concurrent.TimeUnit;

// Thread to send PUTCHUNK message
public class SendPutchunkThread implements Runnable {

  private BackupMessage message;
  private String fileID;
  private int chunkNo;
  private int replicationDeg;
  private int interval;
  private int count;

  // Thread constructor
  public SendPutchunkThread(BackupMessage message, String fileID, int chunkNo, int replicationDeg, int interval){
    this.message = message;
    this.fileID = fileID;
    this.chunkNo = chunkNo;
    this.interval=interval;
    this.replicationDeg = replicationDeg;
    this.count = 0;
  }

  // send message 
  public void run(){
    int numberOccurrences = Peer.getStorage().getOcurr(fileID, chunkNo);
    if(numberOccurrences < replicationDeg){
      message.send();
      this.interval = 2 * this.interval;
      
      count++;
      if (this.count < 5)
        Peer.exec.schedule(this, this.interval, TimeUnit.SECONDS);
    }
  }
  
}
