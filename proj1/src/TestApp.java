import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//package client;

// Client
public class TestApp{

  public static void main(String[] args){
    if (args.length < 2 || args.length > 4){
      System.err.println("Number of arguments invalid, run like this:\n java TestApp <peer_ap> <sub_protcol> <opnd_1> <opnd_2>");
      System.exit(1);
    }

    String remoteName = args[0].trim();
    String argument = args[1].trim();

    try{
      // Connects to RMI 
      Registry registry = LocateRegistry.getRegistry("localhost");
      RemoteInterface stub = (RemoteInterface) registry.lookup(remoteName);
      String filepath=new String();
      switch(argument){
        case "BACKUP": 
          if (args.length != 4) {
            System.out.println("Backup format should be: BACKUP <file_path> <replication_degree>");
            return;
          }
          filepath = args[2];
          int replicationDeg = Integer.parseInt(args[3]);
          stub.backup(filepath, replicationDeg);
          break;

        case "DELETE":
          if (args.length != 3) {
            System.out.println("Delete format should be: DELETE <file_path>");
            return;
          }
          filepath = args[2];
          stub.delete(filepath);
          break;

        case "RESTORE":
          if (args.length != 3) {
            System.out.println("Restore format should be: RESTORE <file_path>");
            return;
          }
          filepath = args[2];
          stub.restore(filepath);
          break;

        case "RECLAIM":
          if (args.length != 3) {
            System.out.println("Reclaim format should be: RECLAIM <space_to_reclaim>");
            return;
          }
          stub.reclaim(Integer.parseInt(args[2]));
          break;

        case "STATE":
          if (args.length != 2) {
            System.out.println("State format should be:  STATE");
            return;
          }
          stub.state();
          break;
        default:
          break;
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}