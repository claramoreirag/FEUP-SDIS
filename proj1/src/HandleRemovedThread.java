import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// Thread that handles REMOVED messages
public class HandleRemovedThread implements Runnable{

    private String fileId;
    private int chunkNo;

    // Thread constructor
    public HandleRemovedThread(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    //TODO: clara, comenta isto xd
    @Override
    public void run() {
        int i =Peer.storage.hasChunk(fileId, chunkNo);
        if (i!=-1) {
            int desiredReplicationDegree = Peer.getStorage().getStoredChunks().get(i).getWantedReplicationDegree();
            String key = fileId + '_' + chunkNo;
            if (Peer.getStorage().getStoredOccurr().get(key) < desiredReplicationDegree){
              int sizeOfChunks = 64000;
                byte[] buffer = new byte[sizeOfChunks];
                byte[] body = new byte[sizeOfChunks];

                //opens chunk file and reads its content to body
                File file = new File(Peer.getId() + "/" + fileId + "_" + chunkNo);
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    int bytesAmount;
                    while ((bytesAmount = bis.read(buffer)) > 0) {
                        body = Arrays.copyOf(buffer, bytesAmount);
                        buffer = new byte[sizeOfChunks];
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                BackupMessage message=new BackupMessage(Peer.getVersion(), Peer.getId(), fileId, body,chunkNo, desiredReplicationDegree);


                if (!Peer.getStorage().getStoredOccurr().containsKey(key)) { //if this chunk ins's in the current replication degrees table
                    Peer.getStorage().getStoredOccurr().put(key, 0); //the chunk is added to that table
                }

                message.send();
                Peer.exec.schedule(new SendPutchunkThread(message,  fileId, chunkNo, desiredReplicationDegree,1), 1, TimeUnit.SECONDS);
            
    
            }

            
        
        }
    }

}