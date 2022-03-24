// Thread that handles GETCHUNK messages received
public class HandleGetchunkThread implements Runnable{
    
    private String fileID;
    private int chunkNo;

    // Thread constructor
    public HandleGetchunkThread(String fileID, int chunkNo) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    // verifies if the peer has the chunk asked and if it has already been sent
    // sends a CHUNK message with the chunk asked for
    @Override
    public void run() {
        Chunk wantedChunk=Peer.storage.getWantedChunk(fileID, chunkNo);
        if (wantedChunk.getId()!=-1 && Peer.storage.needToSendChunk(fileID, chunkNo) ){
            ChunkMessage message= new ChunkMessage(Peer.getVersion(),Peer.getId(),wantedChunk.getFileID(),wantedChunk);
            message.send();
        }

    }
}