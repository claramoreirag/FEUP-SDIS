import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


// Class that represents storage of each peer
public class FileStorage implements Serializable {

    private static final long serialVersionUID = -5381052468361954592L;

    // available space in storage
    private int availableSpace;

    // files in storage
    private ArrayList<FileInfo> files;

    // chunks received 
    private ArrayList<Chunk> receivedChunks;

    // chunks stored
    private ArrayList<Chunk> storedChunks;

    private int occupiedSpace=0;
   
    // string: fileID_chunkNo
    // integer: number of stored occurrences
    private ConcurrentHashMap<String, Integer> storedOccurr;

    // string: fileID_chunkNo
    // boolean: false if storage doesn't have the chunk, true if storafe has the chunk
    // serves to store the chunks the peer asked in RESTORE
    private ConcurrentHashMap<String, Boolean> wantedChunks;

    // string: fileID_chunkNo
    // boolean: true -> there's no need to send the chunk
    // serves to store the chunks that have been received already has CHUNK
    private ConcurrentHashMap<String, Boolean> noNeedtoSendChunks;

    // string: fileID
    // ArrayList<Integer>: list of peer ids that have chunks of that file
    // serves to store the file and the peers that have chunks of that file
    private ConcurrentHashMap<String, ArrayList<Integer>> filesStoredinPeers;

    // list to store the files that need to be deleted in all peers
    private ArrayList<String> filestoDelete;
 

    // Class constructor
    public FileStorage() {
        this.files = new ArrayList<>();
        this.storedChunks = new ArrayList<>();
        this.receivedChunks = new ArrayList<>();
        this.wantedChunks = new ConcurrentHashMap<>();
        this.storedOccurr= new ConcurrentHashMap<>();
        this.noNeedtoSendChunks = new ConcurrentHashMap<>();
        this.availableSpace = Integer.MAX_VALUE;
        this.filesStoredinPeers = new ConcurrentHashMap<>();
        this.filestoDelete = new ArrayList<>();
        
    }

    // add file to storage
    public void addFile(FileInfo f) {
        this.files.add(f);
    }

    // gets received chunks
    public ArrayList<Chunk> getReceivedChunks() {
        return this.receivedChunks;
    }


    // stores chunk by updating the data structures and writing the chunk in the peer folder
    public synchronized boolean storeChunk(Chunk chunk){
        if (!addStoredChunk(chunk)) { //if the peer already has that chunk
            return false;
        }
        takeSpace(chunk.getSize());
        try {
            String filepath= Peer.getId() + "/" + chunk.getFileID() + "_" + chunk.getId();
            File file = new File(filepath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            
            // writing chunk
            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                FileChannel filechannel = fos.getChannel();
                filechannel.write(ByteBuffer.wrap(chunk.getInfo()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // adds occurrence for this chunk
        addOcurr(chunk.getFileID(),chunk.getId());

        // updates the occupied space
        occupiedSpace+=chunk.getSize();
       
        return true;
    }

    // verifies if the chunk is stored in the storage
    public synchronized boolean isStored(String fileId, int chunkNo){
       
        for (Chunk storedChunk : this.storedChunks) {
            if (storedChunk.getFileID().equals(fileId) && (storedChunk.getId() == chunkNo))
                return true;
        }
        return false;
    }

    // adds chunk to stored chunks
    public synchronized boolean addStoredChunk(Chunk chunk) {

        for (Chunk storedChunk : this.storedChunks) {
            if (storedChunk.getFileID().equals(chunk.getFileID()) && storedChunk.getId() == chunk.getId())
                return false;
        }
        this.storedChunks.add(chunk);
        return true;
    }

    // deletes chunk from stored chunks by removing it from the data structure and deleting the file
    public void deleteStoredChunks(String fileID, int senderId) {
        for (Iterator<Chunk> it = this.storedChunks.iterator(); it.hasNext(); ) {
            Chunk chunk = it.next();
            if (chunk.getFileID().equals(fileID)) {
                String filepath = Peer.getId() + "/" + fileID + "_" + chunk.getId();  
                File file = new File(filepath);
                file.delete();
                removeStoredEntry(fileID, chunk.getId());
                freeSpace(fileID, chunk.getId());
                it.remove();
                System.out.println("Deleting chunk asked by peer"+ senderId);
            }
        }
    }

    // adds occurrence to chunk
    public synchronized void addOcurr(String fileID, int chunkNo) {
        String key = fileID + '_' + chunkNo;

        if (!Peer.storage.getStoredOccurr().containsKey(key)) {
            Peer.storage.getStoredOccurr().putIfAbsent(key, 1);
        } else {
            int total = this.storedOccurr.get(key) + 1;
            this.storedOccurr.replace(key, total);
        }
    }

    // gets chunk occurrence
    public synchronized int getOcurr(String fileID, int chunkNo){
        String key = fileID + '_' + chunkNo;
        if (!Peer.storage.getStoredOccurr().containsKey(key)) {
            Peer.storage.getStoredOccurr().putIfAbsent(key, 0);
        }
       
        return storedOccurr.get(key);
    }

    // gets stored occurrence data structure
    ConcurrentHashMap<String, Integer> getStoredOccurr() {
        return storedOccurr;
    }

    // takes one occurrence from chunk
    public synchronized void takeStoredOccurr(String fileID, int chunkNo) {
        String key = fileID + '_' + chunkNo;
        int total = this.storedOccurr.get(key) - 1;
        this.storedOccurr.replace(key, total);
    }

    // removes chunk from stored occurrences
    public synchronized void removeStoredEntry(String fileID, int chunkNo){
        String key = fileID + '_' + chunkNo;
        this.storedOccurr.remove(key);
    }

    // for every stored chunk, gets his currents replication degree from the stored occurences table
    public void fillCurrReplDegChunks() {
        for (Chunk storedChunk : this.storedChunks) {
            String key = storedChunk.getFileID() + "_" + storedChunk.getId();
            storedChunk.setCurrentReplicationDegree(this.storedOccurr.get(key));
        }
    }

    // adds wanted chunk 
    public void addWantedChunk(String fileID, int chunkNo) {
        String key = fileID + '_' + chunkNo;
        this.wantedChunks.putIfAbsent(key, false);
    }

    // sets wanted chunk as received
    public void setWantedChunkReceived(String fileID, int chunkNo) {
        String key = fileID + '_' + chunkNo;
        this.wantedChunks.replace(key, true);
    }

    // returns chunk object in stored chunks with fileID and chunkNo
    public synchronized Chunk getWantedChunk( String fileID, int chunkNo){
        for(Chunk c:storedChunks){
            if(c.getFileID().equals(fileID) && chunkNo==c.getId()){
                return c;
            }
        }
        return new Chunk();
    }

    // gets available space
    public synchronized int getAvailableSpace() {
        return this.availableSpace;
    }

    // sets available space
    public synchronized void setSpaceAvailable(int space) {
        this.availableSpace = space;
    }

    // takes available space corresponding to a chunk size
    public synchronized void takeSpace(int chunkSize){
        availableSpace -= chunkSize;
    }

    // frees space corresponding to chunk size
    public synchronized void freeSpace(String fileId, int chunkNo){
        for (Chunk storedChunk : this.storedChunks) {
            if (storedChunk.getId() == chunkNo && storedChunk.getFileID().equals(fileId) )
                this.availableSpace += storedChunk.getSize();
                occupiedSpace-=storedChunk.getSize();
        }
    }

    // calculates space used by chunks
    public synchronized int getFilledSpace(){
        int filled = 0;
        for (Chunk storedChunk : this.storedChunks) {
            filled  += storedChunk.getSize();
        }
        return filled;
    }

    // sets available space
    public void setAvailableSpace(int availableSpace) {
        this.availableSpace = availableSpace;
    }

    // gets files
    public ArrayList<FileInfo> getFiles() {
        return files;
    }

    // add chunk to received chunks
    public Boolean addReceivedChunk(Chunk chunk){
        for(Chunk c : receivedChunks){
            if(c.getFileID().equals(chunk.getFileID()) && c.getId() == chunk.getId())
                return false;
        }
        receivedChunks.add(chunk);
        return true;
    }

    // returns file that has the same path has filePath    
    public FileInfo getFilebyPath(String filePath){
        for( FileInfo f: files){
            if(f.getFilepath().equals(filePath)){
                return f;
            }
        }
        return new FileInfo(null, 1);
    }

    // get wanted chunks data structure
    public ConcurrentHashMap<String, Boolean> getWantedChunks() {
        return wantedChunks;
    }

    // sets wanted chunks data structure
    public void setWantedChunks(ConcurrentHashMap<String, Boolean> wantedChunks) {
        this.wantedChunks = wantedChunks;
    }

    // checks if the chunk is wanted
    public boolean checkIfWanted(String fileID,int chunkNo){
        String key=fileID+'_'+chunkNo;
        return wantedChunks.containsKey(key);
    }

    // adds chunk to no need to send chunk
    public void addNoNeedtoSendChunk(String fileID, int chunkNo){
        String key = fileID + '_' + chunkNo;
        this.noNeedtoSendChunks.putIfAbsent(key, true);
    }

    // verifies if there's need to send chunk
    public boolean needToSendChunk(String fileID, int chunkNo){
        String key = fileID + '_' + chunkNo;
        return !noNeedtoSendChunks.contains(key);
    }

    // gets occupied space
    public  int getOccupiedSpace() {
        return occupiedSpace;
    }

    // gets stored chunks
	public ArrayList<Chunk> getStoredChunks() {
		return storedChunks;
	}

	// save storage in file
    public void save() {
        try {
            String filename = Peer.getId() + "/storage.ser";

            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    // gets files stored in peers
    public ConcurrentHashMap<String, ArrayList<Integer>> getFilesStoredinPeers(){
        return filesStoredinPeers;
    }

    // add file in peer
    public void addFileinPeer(String fileId, int peerId){ 
        if(filesStoredinPeers.containsKey(fileId)){
            if(!filesStoredinPeers.get(fileId).contains(peerId))
                filesStoredinPeers.get(fileId).add(peerId);
        }
        else{
            ArrayList<Integer> peerList = new ArrayList<>();
            peerList.add(peerId);
            filesStoredinPeers.putIfAbsent(fileId, peerList);
        }

    }

    // delete file in peer
    public void deleteFileinPeer(String fileId, int peerId){ //Update in DELETED
        filesStoredinPeers.get(fileId).remove(new Integer(peerId));
        if(filesStoredinPeers.get(fileId).isEmpty()){
            filestoDelete.remove(fileId);
            filesStoredinPeers.remove(fileId);
        }
    }

    // gets files to delete
    public ArrayList<String> getFilestoDelete(){
        return filestoDelete;
    }

    // add file to delete
    public void addFiletoDelete(String fileId){
        if(!filestoDelete.contains(fileId))
        {
            filestoDelete.add(fileId);
        }          
    }

    // list with file ids that peer needs to delete
    public ArrayList<String> peerNeedstoDeleteFile(int peerId){
        ArrayList<String> result = new ArrayList<>();
        for(String fileId : filestoDelete){
            if(filesStoredinPeers.containsKey(fileId)){
                if(filesStoredinPeers.get(fileId).contains(peerId)){
                    result.add(fileId);
                }
            }      
        }
        return result;
    }

    // set occupied space
    public void setOccupiedSpace(int occupiedSpace) {
        this.occupiedSpace = occupiedSpace;
    }

    // verifies if storage has chunk
    public int hasChunk(String fileID, int chunkNo){
        int i=0;
        for(Chunk c:storedChunks){
            if(fileID.equals(c.getFileID())&& chunkNo==c.getId())return i;
            i++;
        }
        return -1;
    }
}

