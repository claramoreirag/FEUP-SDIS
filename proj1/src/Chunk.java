import java.io.Serializable;

//Class that represents a chunk
public class Chunk implements Serializable {
  
  private int id;
  private String fileID;
  private byte[] info;
  private int size;
  private int wantedReplicationDegree;
  private int currentReplicationDegree;

  //Class constructor
  public Chunk(int id, String fileID, byte[] info, int size, int wantedReplicationDeg){
    this.id = id;
    this.fileID = fileID;
    this.wantedReplicationDegree=wantedReplicationDeg;
    this.info = info;
    this.size = size;
    this.currentReplicationDegree=0;
  }

  //Class constructor
  public Chunk(int id, String fileID, int wantedReplicationDeg, int size){
    this.id = id;
    this.fileID = fileID;
    this.wantedReplicationDegree = wantedReplicationDeg;
    this.size = size;
    this.currentReplicationDegree=0;
  }

  //Class constructor just to initialize object
  public Chunk() {
    this.id=-1;
  }

//Class constructor
  public Chunk(int chunkNo, String fileID2, byte[] body, int length) {
    this.id = chunkNo;
    this.fileID = fileID2;
    this.info = body;
    this.size = length;
    this.currentReplicationDegree=0;
  }

  //gets chunk id
  public int getId() {
    return id;
  }

  // sets chunk id
  public void setId(int id) {
    this.id = id;
  }

  // gets file id
  public String getFileID() {
    return fileID;
  }

  // sets file id
  public void setFileID(String fileID) {
    this.fileID = fileID;
  }

  // gets chunk's body aka info
  public byte[] getInfo() {
    return info;
  }

  // sets chunk's body aka indo
  public void setInfo(byte[] info) {
    this.info = info;
  }

  // gets chunk size
  public int getSize() {
    return size;
  }

  // sets chunk size
  public void setSize(int size) {
    this.size = size;
  }

  // gets wanted replication degree
  public int getWantedReplicationDegree() {
    return wantedReplicationDegree;
  }

  // sets wanted replication degree
  public void setWantedReplicationDegree(int wantedReplicationDegree) {
    this.wantedReplicationDegree = wantedReplicationDegree;
  }

  // gets current replication degree
  public int getCurrentReplicationDegree() {
    return currentReplicationDegree;
  }

  //sets current replication degree
  public void setCurrentReplicationDegree(int currentReplicationDegree) {
    this.currentReplicationDegree = currentReplicationDegree;
  }
  
}
