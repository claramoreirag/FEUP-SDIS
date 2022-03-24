import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Thread that handles all CHUNK messages received
public class HandleChunkThread implements Runnable {

    String filename;

    // Thread constructor
    public HandleChunkThread(String filename) {
        this.filename = filename;
    }
    
    // verifies if all chunks needed were sent and buils file to restore
    @Override
    public void run() {
        if (!Peer.storage.getWantedChunks().containsValue(false)) {
            if (buildFile())
                System.out.println("The file was restored!\n");
            else System.out.println("ERROR: File not restored :(\n");
        } else System.out.println("ERROR: File not restored, chunks missing :(\n");

        //updates wanted chunks
        Peer.storage.getWantedChunks().clear();

    }

    // builds file with chunks received
    private boolean buildFile() {
        String filepath = Peer.getId() + "/" + this.filename;
        File file = new File(filepath);
       
        try {

            // creates file if needed
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file, true);

            //orders chunks by ID
            Peer.storage.getReceivedChunks().sort((c1, c2) -> {
                return Integer.compare(c1.getId(),c2.getId());
            });

            // writes chunks
            for(Chunk c:Peer.storage.getReceivedChunks()){
               
                outputStream.write(c.getInfo());

            }

            // clears received chunks
            Peer.storage.getReceivedChunks().clear();
            outputStream.close();
            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }   
}