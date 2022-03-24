//package interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Remote Interface 
public interface RemoteInterface extends Remote{
    
    // Backup function
    void backup(String filepath, int replicationDegree) throws RemoteException;

    // Restore function
    void restore(String filepath) throws RemoteException;

    // Delete function
    void delete(String filepath) throws RemoteException;

    // Reclaim function
    void reclaim(int storageSpace) throws RemoteException;

    // State function
    void state() throws RemoteException;
    
}