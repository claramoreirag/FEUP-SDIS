// Thread to send messages
public class SendMessageThread implements Runnable {
    
    private String channel;
    private byte[] message;

    // Thread constructor
    public SendMessageThread(byte[] message, String channel) {
        this.message = message;
        this.channel = channel;
    }

    // Send message depending on argument on which channel
    public void run() {
        switch (channel) {
            case "control":
                Peer.getControlChannel().send(this.message);
                break;
            case "backup":
                Peer.getBackupChannel().send(this.message);
                break;
            case "restore":
                Peer.getRestoreChannel().send(this.message);
                break;
        }
    }
}