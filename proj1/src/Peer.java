import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
//package peer;
import java.util.concurrent.TimeUnit;

// Peer class
public class Peer implements RemoteInterface {

    private static int id;
    private static String version;
    private static ControlChannel controlChannel;
    private static BackupChannel backupChannel;
    private static RestoreChannel restoreChannel;
    public static ScheduledThreadPoolExecutor exec;
    public static FileStorage storage;
    private static EndThread endThread;
    

    private  static final int N_THREADS_PER_CHANNEL = 15;    /** number of threads ready for processing packets in each channel */

   // gets control channel
    public static ControlChannel getControlChannel() {
        return controlChannel;
    }

    // gets backup channel
    public static BackupChannel getBackupChannel() {
        return backupChannel;
    }

    // gets restore channel
    public static RestoreChannel getRestoreChannel() {
        return restoreChannel;
    }

    // gets storage
    public static FileStorage getStorage(){
        return storage;
    }

    // invalid arguments print
    public static void printUsage(){
        System.out.println("Invalid Arguments\n"+"Usage must be: Peer" +
                    "<Version> <peerID> <accessPoint> <controlAddress> <controlPort> <backupAddress> <backupPort> <restoreAddress> <restoreControlPort> ");
    }

    // Peer constructor -> initiate channels, storage and thread pool executor
    private Peer(String controlAddress, int controlPort, String backupAddress, int backupPort, String restoreAddress, int restorePort) {
        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(N_THREADS_PER_CHANNEL);
        controlChannel = new ControlChannel(controlAddress, controlPort);
        backupChannel = new BackupChannel(backupAddress, backupPort);
        restoreChannel = new RestoreChannel(restoreAddress, restorePort);
        storage=new FileStorage();
    }

    // gets id
    public static int getId() {
		return id;
	}

    public static void main(String args[]) throws IOException {

        System.setProperty("java.net.preferIPv4Stack", "true");

        if (args.length != 9) {
            printUsage();
            return;
        }

        // parse arguments
        version = String.valueOf(Double.parseDouble(args[0]));
        id = Integer.parseInt(args[1]);
        String accessPoint = args[2];
        String controlAddr = args[3];
        int controlP = Integer.parseInt(args[4]);
        String backupAddr = args[5];
        int backupP = Integer.parseInt(args[6]);
        String restoreAddr = args[7];
        int restoreP = Integer.parseInt(args[8]);


        try {
            // connection to RMI
            Peer obj = new Peer(controlAddr,controlP, backupAddr,backupP, restoreAddr, restoreP);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);
            System.out.println("Server ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start executing channels
        loadStorage();
        endThread=new EndThread();
        endThread.run();
        exec.execute(controlChannel);
        exec.execute(backupChannel);
        exec.execute(restoreChannel);
        
        if(version.equals("2.0")){
            // send awake message
            AwakeMessage message = new AwakeMessage(String.valueOf(version), id);
            message.send();
        }
    }

    // backup function
    @Override
    public void backup(String filename, int replicationDegree) throws RemoteException {
        if (filename == null || replicationDegree < 1 || replicationDegree > 9) {
            throw new IllegalArgumentException("Backup: Invalid arguments");
        }
        String filepath=filename;
        FileInfo file = new FileInfo(filepath,replicationDegree);
        storage.addFile(file);
        System.out.println("Asking for backup of file "+ filename);
        // for each chunk, send putchunk message
        ArrayList<Chunk> chunks=file.splitFilesIntoChucks();
        for(int i = 0; i < file.getNumberChunks();i++){
            BackupMessage message = new BackupMessage(version, id, file.getID(), chunks.get(i));
            message.send();
           
            exec.schedule(new SendPutchunkThread(message, file.getID(),chunks.get(i).getId(),replicationDegree,1), 1, TimeUnit.SECONDS);
        }       
    }

    // restore function
    @Override
    public void restore(String filename) throws RemoteException {
        FileInfo file=storage.getFilebyPath(filename);
        if(file.getFilepath()!=null){
            // for each chunk, send restore message
            System.out.println("Asking to restore file "+ filename);
            for(int i = 0; i < file.getNumberChunks();i++){
                storage.addWantedChunk(file.getID() ,i);
                RestoreMessage message = new RestoreMessage(version, id, file.getID(), i);
                message.send();
            }
            Peer.exec.schedule(new HandleChunkThread(filename), 4, TimeUnit.SECONDS);
        }
    }

    // delete function
    @Override
    public void delete(String filename) throws RemoteException {
        
        FileInfo temp = new FileInfo(filename, 0);
        System.out.println("Asking to delete file "+ filename);
        // sends 6 times the delete message for the file
        for(int j = 0; j < 6; j++){
            DeleteMessage message= new DeleteMessage(version,id,temp.getID());
            message.send();
        }
    }

    // reclaim function
    @Override
    public void reclaim(int storageSpace) throws RemoteException {
        int spaceToReclaim = storage.getOccupiedSpace() - storageSpace; //calculating the space needeed to clear
        System.out.println("Reclaiming space: the new available space will be " + storageSpace+ "bytes");
        if (spaceToReclaim > 0) {
            int spaceFreed = 0;
            for (Iterator<Chunk> iter = storage.getStoredChunks().iterator(); iter.hasNext(); ) {
                System.out.println(spaceFreed);
                Chunk chunk = iter.next();

                if (spaceFreed <= spaceToReclaim) { //if the space occupied by the deleted chunks wasn't enough, keep deleting
                    spaceFreed = spaceFreed + chunk.getSize();

                    RemovedMessage message =new RemovedMessage(Peer.getVersion(),id,chunk.getFileID(),chunk.getId());
                    message.send();
    
                    String filename = Peer.getId() + "/" + chunk.getFileID() + "_" + chunk.getId();
                    File file = new File(filename);
                    file.delete();
                    storage.setOccupiedSpace(storage.getOccupiedSpace()-chunk.getSize());
                    Peer.getStorage().takeStoredOccurr(chunk.getFileID(), chunk.getId()); //decrements the stored occurrences of the deleted chunk
                    iter.remove();
                } else {
                    break;
                }
            }
        }
    }

    // state function
    @Override
    public void state() throws RemoteException {
        System.out.println("\nSTATE");
        System.out.println("\nFILES IT ASKED TO BACKUP\n");
        if(storage.getFiles().size()==0){
            System.out.println("No files backed up yet!");
        }
        else{
            for (int i = 0; i < storage.getFiles().size(); i++) {
                String fileID = storage.getFiles().get(i).getID();
    
                System.out.println("FILE PATHNAME: " + storage.getFiles().get(i).getFilepath());
                System.out.println("FILE ID: " + fileID);
                System.out.println("FILE DESIRED REPLICATION DEGREE: " + storage.getFiles().get(i).getReplicationDegree() + "\n");
                System.out.println("\nFILE CHUNKS \n" );
                for (int j = 0; j < storage.getFiles().get(i).getNumberChunks(); j++) {
                  
                    String key = fileID + '_' + j;
                    System.out.println("CHUNK ID: " + j);
                    System.out.println("DESIRED REPLICATION DEGREE: " + storage.getFiles().get(i).getReplicationDegree() + "\n");
                    System.out.println("PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurr().get(key) + "\n");
                }
            }
        }
        
        System.out.println("\n\nCHUNKS IT STORES\n");

        if(storage.getStoredChunks().size()==0){
            System.out.println("No chunk stored yet!");
        }
        else{
            for (int i = 0; i < storage.getStoredChunks().size(); i++) {
                int chunkNo = storage.getStoredChunks().get(i).getId();
                String key = storage.getStoredChunks().get(i).getFileID() + '_' + chunkNo;
                System.out.println("CHUNK ID: " + chunkNo +  "\t FROM FILE: "+ storage.getStoredChunks().get(i).getFileID());
                System.out.println("DESIRED REPLICATION DEGREE: " + storage.getStoredChunks().get(i).getWantedReplicationDegree() + "\n");
                System.out.println("PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurr().get(key) + "\n");
            }
        }
    }

    // loads store from file
	private static void loadStorage() {
        try {
            String filename = Peer.getId() + "/storage.ser";

            File file = new File(filename);
            if (!file.exists()) {
                storage = new FileStorage();
                return;
            }

            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storage = (FileStorage) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
    }

    // gets version
    public static String getVersion() {
        return version;
    }

    // sets version
    public static void setVersion(String version) {
        Peer.version = version;
    }
    
}