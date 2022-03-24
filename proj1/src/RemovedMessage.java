import java.io.UnsupportedEncodingException;

// Class that represents REMOVE message and sends it
public class RemovedMessage extends Message{
    private int chunkNo;
   
    // Class constructor
    public RemovedMessage(String version, int senderId, String fileId, int chunkNo) {
        super(version, senderId, fileId);
        this.chunkNo = chunkNo;
    }

    // builds REMOVE header
    @Override
    protected byte[] buildHeader() throws UnsupportedEncodingException {
        String header = version +" REMOVED" + " " + senderId + " " + hashedfileId + " " + chunkNo + "\r\n\r\n";
        byte[] headerBytes = header.getBytes("US-ASCII");
        return headerBytes;    
    }

    // builds REMOVE message
    protected byte[] build() {
        try {
            byte[] message =buildHeader();
            return message;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[64500];
    }

    // sends message
    @Override
    protected void send() {
        byte[] message = build();
        System.out.println("Removed chunk "+ chunkNo+ " from file"+ hashedfileId );
        SendMessageThread sendThread = new SendMessageThread(message, "control");
        Peer.exec.execute(sendThread);
     

    }
    
}
