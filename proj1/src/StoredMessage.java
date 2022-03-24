import java.io.UnsupportedEncodingException;

// Class that represents STORED message and sends it
public class StoredMessage extends Message {
    private int chunkNo;
   
    // Class constructor
    public StoredMessage(String version, int senderId, String fileId, int chunkNo) {
        super(version, senderId, fileId);
        
        this.chunkNo = chunkNo;
    }

    // Builds STORED header
    @Override
    protected byte[] buildHeader() throws UnsupportedEncodingException {
        String header = version +" STORED" + " " + senderId + " " + hashedfileId + " " + chunkNo + "\r\n\r\n";
        byte[] headerBytes = header.getBytes("US-ASCII");
        return headerBytes;
        
    }

    // Builds STORED message
    protected byte[] build() {
        try {
            
            byte[] message =buildHeader();
       
            return message;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[64500];
    }

    // send message
    @Override
    protected void send() {
        String header = version +" STORED" + " " + senderId + " " + hashedfileId + " " + chunkNo + "\r\n\r\n";
        byte[] message = build();
        System.out.println("Stored chunk " + chunkNo + " from file "+ hashedfileId);
        SendMessageThread sendThread = new SendMessageThread(message, "control");
        Peer.exec.execute(sendThread);
     

    }
    
}