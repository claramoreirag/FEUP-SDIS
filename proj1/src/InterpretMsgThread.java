import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Thread that deals with all kinds of received messages
public class InterpretMsgThread implements Runnable {
  
  private byte[] message;
  private String version;
  private int senderID;
  private String fileID;
  private int chunkNo;
  private int replicationDegree;
  private byte[] body;

  // Thread constructor
  public InterpretMsgThread(byte[] message){
    this.message = message;  
  }

  // runs thread and calls specific function dependending on message argument
  public void run(){
    String messagetr = new String(this.message);
    String[] messageArr = messagetr.trim().split(" ");
    String argument = messageArr[1];  // gets argument
   
    switch(argument){
      case "PUTCHUNK":
        handlePutchunk();
        break;
      case "STORED":
        handleStored();
        break;
      case "GETCHUNK":
        handleGetChunk();
        break;
      case "CHUNK":
        handleChunk();
        break;
      case "DELETE":
        handleDelete();
        break;
      case "REMOVED":
        handleRemoved();
        break;
      case "DELETED":
        if(Peer.getVersion().equals("2.0"))handleDeleted();
        break;
      case "AWAKE":
        if(Peer.getVersion().equals("2.0"))handleAwake();
        break;
      default:
        break;
    }
  }

  // handles PUTCHUNK message by calling handle putchunk thread
  public void handlePutchunk(){
    decomposeBackupHeaderandBody();
    if (Peer.getId() != senderID) { //if the peer who sent the message isnt the one who is receiving it
      Random random = new Random();
      Peer.exec.schedule(new HandlePutchunkThread(version,senderID, fileID, chunkNo, replicationDegree, body), random.nextInt(401), TimeUnit.MILLISECONDS);
    }
  
  }

  // handles STORED messages
  public void handleStored(){
    decomposeStoredHeader();
    if(Peer.getId()!=senderID)Peer.getStorage().addOcurr(fileID, chunkNo);
    Peer.storage.addFileinPeer(fileID, senderID);
  }

  // handles REMOVED messages
  public void handleRemoved(){
    decomposeRemovedHeader();
    if (Peer.getId() != senderID) {  //if the peer who sent the message isnt the one who is receiving it
      Peer.getStorage().takeStoredOccurr(fileID, chunkNo); //decrements de current replication degree of this chunk
      System.out.println("Received REMOVED " + version + " " + senderID + " " + fileID + " " + chunkNo);
      Random random = new Random();
      Peer.exec.schedule(new HandleRemovedThread(fileID, chunkNo), random.nextInt(401), TimeUnit.MILLISECONDS);
    }
  }

  // handles GETCHUNK messages
  public void handleGetChunk(){
    decomposeGetchunkHeader();
    if (Peer.getId() != senderID) { //if the peer who sent the message isnt the one who is receiving it
      Random random = new Random();
      Peer.exec.schedule(new HandleGetchunkThread(fileID, chunkNo), random.nextInt(401), TimeUnit.MILLISECONDS);
    }
  }

  // handles CHUNK messages
  public void handleChunk(){
    decomposeChunkHeaderandBody();
    if(Peer.getId() != senderID){ 
      Chunk chunk = new Chunk(chunkNo, fileID, body, body.length);
      if(Peer.storage.checkIfWanted(fileID, chunkNo)){ //verifies if chunk is wanted
        Peer.storage.setWantedChunkReceived(fileID, chunkNo); // makes chunk wanted received
        Peer.storage.addReceivedChunk(chunk); // adds chunk to received chunks
      }
      else{
        Peer.storage.addNoNeedtoSendChunk(fileID, chunkNo); // this chunk doesn't need to be sent
      }
    }
  }

  // handles DELETE message
  public void handleDelete(){
    decomposeDeleteHeader();
    Peer.storage.deleteStoredChunks(fileID,senderID); //deletes chunk in stored chunks
    Peer.storage.addFiletoDelete(fileID); //add file to files to delete
    
    DeletedMessage message = new DeletedMessage(version, senderID, fileID); 
    message.send(); //sends message
  
  }

  // handles DELETED message
  public void handleDeleted(){
    decomposeDeleteHeader();
    Peer.storage.deleteFileinPeer(fileID, senderID); // takes file from peer
  }

  // handles AWAKE message
  public void handleAwake(){
    decomposeAwakeHeader();
    ArrayList<String> filesToDelete =  Peer.storage.peerNeedstoDeleteFile(senderID); //gets files to delete
    if(!filesToDelete.isEmpty()){
      Random random = new Random();
      Peer.exec.schedule(new SendDeleteMessageThread(filesToDelete, version, senderID), random.nextInt(401), TimeUnit.MILLISECONDS);
    }
  }

  // separates header from body
  private List<byte[]> separateHeaderFromBody(){
    int i;
    for (i = 0; i < this.message.length - 4; i++) {
      if (this.message[i] == 0xD && this.message[i + 2] == 0xD && this.message[i + 1] == 0xA  && this.message[i + 3] == 0xA) {
        break;
      }
    }

    byte[] header = Arrays.copyOfRange(this.message, 0, i);
    //byte[] body = new byte[64000];
    //int bodyLen= 64004+header.length;
    //System.arraycopy(message, i, body, 0, body.length);
    byte[] body=Arrays.copyOfRange(this.message, i + 4,this.message.length );
    String s = new String(body, StandardCharsets.UTF_8);
    //System.out.println("message body: "+s);
    List<byte[]> division = new ArrayList<>();
    
    division.add(header);
    division.add(body);
 
    return division;
  }

  // gets arguments and body for backup
  private void decomposeBackupHeaderandBody(){
    List<byte[]> headerBody=separateHeaderFromBody();
    byte[] header = headerBody.get(0);
    this.body = headerBody.get(1);
    String headertoStr = new String(header);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
    this.chunkNo = Integer.parseInt(strArrayHeader[4].trim());
    this.replicationDegree = Integer.parseInt(strArrayHeader[5].trim());   
  }

  // gets arguments from delete header
  private void decomposeDeleteHeader(){
    String headertoStr = new String(message);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
  }

  // gets arguments from stored header
  private void decomposeStoredHeader(){
    String headertoStr = new String(message);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
    this.chunkNo = Integer.parseInt(strArrayHeader[4].trim());
  }

  // gets arguments from getchunk header
  private void decomposeGetchunkHeader(){
    String headertoStr = new String(message);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");
    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
    this.chunkNo = Integer.parseInt(strArrayHeader[4].trim());
  }

  // gets arguments from removed header
  private void decomposeRemovedHeader(){
    String headertoStr = new String(message);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
    this.chunkNo = Integer.parseInt(strArrayHeader[4].trim());
  }

  // gets arguments from awake header
  private void decomposeAwakeHeader(){
    String headertoStr = new String(message);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[1].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
  }

  // gets arguments and body from chunk message
  private void decomposeChunkHeaderandBody(){
    List<byte[]> headerBody=separateHeaderFromBody();
    byte[] header = headerBody.get(0);
    this.body = headerBody.get(1);
    String headertoStr = new String(header);
    String trimmedHeader = headertoStr.trim();
    String[] strArrayHeader = trimmedHeader.split(" ");

    this.version = strArrayHeader[0].trim();
    this.senderID = Integer.parseInt(strArrayHeader[2].trim());
    this.fileID = strArrayHeader[3].trim();
    this.chunkNo = Integer.parseInt(strArrayHeader[4].trim());
  }
}
