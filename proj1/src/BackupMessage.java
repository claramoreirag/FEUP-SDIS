import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

//Class that sends the putchunk message
public class BackupMessage extends Message {
    private int chunkNo;
    private int replicationDegree;
    private byte[] chunkBody;

    //Class constructor
    public BackupMessage(String version, int senderId, String fileId, Chunk chunk) {
        super(version, senderId, fileId);
        this.chunkBody=chunk.getInfo();
        this.chunkNo = chunk.getId();
        this.replicationDegree = chunk.getWantedReplicationDegree();     
    }

    //Class constructor
    public BackupMessage(String version, int senderId, String fileId, byte[] body, int chunkNo,int replicationDegree) {
        super(version, senderId, fileId);
        this.chunkBody=body;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
    }

    
    //Builds putchunk header
    @Override
    protected byte[] buildHeader() throws UnsupportedEncodingException {
        String header = version+ " PUTCHUNK "+ senderId + " " + hashedfileId + " " + chunkNo + " "
                + replicationDegree + "\r\n\r\n";
        byte[] headerBytes = header.getBytes("US-ASCII");
        return headerBytes;
    }

    //Builds entire message: Header + body
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

    //Sends putchunk message
    @Override
    protected void send() {
        byte[] message = build();
        
        SendMessageThread sendThread = new SendMessageThread(message, "backup");
        Peer.exec.execute(sendThread);
        
    }




}