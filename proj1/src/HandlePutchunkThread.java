// Thread that handles PUTCHUNK messages
public class HandlePutchunkThread implements Runnable{

  private String version;
  private int senderID;
  private String fileID;
  private int chunkNo;
  private int replicationDeg;
  private byte[] body;

  // Thread constructor
  public HandlePutchunkThread(String version, int senderID, String fileID, int chunkNo, int replicationDeg, byte[] body){
    this.version = version;
    this.senderID = senderID;
    this.fileID = fileID;
    this.chunkNo = chunkNo;
    this.replicationDeg = replicationDeg;
    this.body = body;
  }

  // stores chunk if possible and sends STORED message
  public void run(){
    //verify if chunk is in peer
    if (Peer.getStorage().isStored(fileID, chunkNo)){
      
      return;
    }
    if(version.equals("2.0") && Peer.storage.getOcurr(fileID, chunkNo)>=replicationDeg){
  
      return;
    }
    // verifies if it can store chunk
    if(Peer.getStorage().getAvailableSpace() >= body.length){
      Chunk chunk = new Chunk(chunkNo, fileID, body, body.length,replicationDeg);
      Peer.getStorage().storeChunk(chunk); // stores chunk

      //sends STORED message
      StoredMessage message =new StoredMessage(version, Peer.getId(), fileID, chunkNo);
      message.send();
    }
  }
}