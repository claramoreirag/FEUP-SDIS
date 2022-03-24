import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.security.MessageDigest;

// Class that represents a file 
public class FileInfo implements Serializable {
  
  private static final long serialVersionUID = -7513418411918342033L;
  private String filepath;
  private static final int CHUNK_SIZE = 64000;
  private String id;

  private int numberChunks;
  private int replicationDegree;
  
  // Class constructor
  public FileInfo(String filepath, int replicationDegree){
    this.filepath=filepath;
    this.replicationDegree = replicationDegree;
    
    getFileID();
    splitFilesIntoChucks();
  }

  // get file replication degree 
  public int getReplicationDegree() {
      return this.replicationDegree;
  }

  // splits files into chunks and sets the number of chunks that represent the file
  public ArrayList<Chunk> splitFilesIntoChucks(){
    
    int chunkID = 0;

    ArrayList<Chunk> chunks = new ArrayList<>();

    // creates buffer to read a chunk size of information
    byte[] buffer = new byte[CHUNK_SIZE];
    File file =new File(this.filepath);
    if(file.exists()){
      try{
        FileInputStream fileStream = new FileInputStream(file);
        BufferedInputStream bufferStream = new BufferedInputStream(fileStream);

        int bytesRead;
        // reads file and divides into chunks
        while((bytesRead = bufferStream.read(buffer)) > 0){
          byte[] content = Arrays.copyOf(buffer, bytesRead);
          Chunk chunck = new Chunk(chunkID, this.id, content, bytesRead, replicationDegree);
          chunks.add(chunck);
          chunkID++;
          buffer = new byte[CHUNK_SIZE];
        }

        if(file.length() % CHUNK_SIZE == 0){
          chunks.add(new Chunk(chunkID, this.id, null, 0,replicationDegree));
        }
        bufferStream.close();
      }
      catch(Exception e){
        e.printStackTrace();
      }    
    }
    else{
      System.out.println("File not found");
    }

    // sets number of chunks
    this.numberChunks = chunks.size();
    
    // returns list of chunks that represent the file
    return chunks;
  }

  // uses hash SHA256 to transform str to file id
  public String toSHA256(String str){

    try{
      MessageDigest md = MessageDigest.getInstance("SHA-256");

      byte[] arr; 
      arr = md.digest(str.getBytes(StandardCharsets.UTF_8));
  
      BigInteger number = new BigInteger(1, arr);
  
      StringBuilder hexString = new StringBuilder(number.toString(16));
  
      while(hexString.length() < 32){
        hexString.insert(0, '0');
      }

      return hexString.toString();   
    }
    catch(Exception e){
      e.printStackTrace();
    }
  
    return "fail";
  }

  // sets file id
  public void getFileID() {
    File file =new File(filepath);
    if(file.exists()){
      String name = file.getName();
      String parent = file.getParent();
      String lastModified = String.valueOf(file.lastModified());

      this.id = toSHA256(name + ":" + parent + ":" + lastModified);
    }
    else{
      System.out.println("File not found");
    }
  }

  // gets file id
  public String getID(){
    return this.id;
  }

  // gets file path
  public String getFilepath() {
    return filepath;
  }

  // sets file path
  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

  // gets number of chunks
  public int getNumberChunks() {
    return numberChunks;
  }

  // sets number of chunks
  public void setNumberChunks(int numberChunks) {
    this.numberChunks = numberChunks;
  }


}
