// Thread that shuts down peers
public class EndThread implements Runnable{

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                Peer.storage.save();
                System.out.println("\nSaving and shutting down gracefully!");
            }
        });
    }
}


