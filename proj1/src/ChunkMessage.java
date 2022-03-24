import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Class that represents a CHUNK message
public class ChunkMessage extends Message {
    private int chunkNo;
    private byte[] chunkBody;
    
    // Class constructor
    public ChunkMessage(String version, int senderId, String fileId, Chunk chunk) {
        super(version, senderId, fileId);
        this.chunkBody=chunk.getInfo();
        this.chunkNo = chunk.getId();  
    }

    // Builds CHUNK header
    @Override
    protected byte[] buildHeader() throws UnsupportedEncodingException {
        String header = version+ " CHUNK "+ senderId + " " + hashedfileId + " " + chunkNo + "\r\n\r\n";
        byte[] headerBytes = header.getBytes("US-ASCII");
        return headerBytes;
    }

    // Builds CHUNK message: header + chunk body
    protected byte[] build() {
        try {
            byte[] header = buildHeader();
            byte[] message = new byte[header.length +chunkBody.length];

            System.arraycopy(header, 0, message, 0, header.length);
            System.arraycopy(chunkBody, 0, message, header.length,chunkBody.length);
          
            return message;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[64200];
    }

    // Sends CHUNK message
    @Override
    protected void send() {
        byte[] message = build();
        
        try {
            
            SendMessageThread sendThread = new SendMessageThread(message, "restore");
            Random random = new Random();
            Peer.exec.schedule(sendThread, random.nextInt(401), TimeUnit.MILLISECONDS); 
            System.out.println("Sending chunk "+ chunkNo+ "from file "+ hashedfileId );         
        } catch (Exception e) {
            
            e.printStackTrace();
        }

    }
}