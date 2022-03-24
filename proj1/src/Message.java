import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.net.*;

// Abstract class to represent message
public abstract class Message {
    protected String version;
    protected int senderId;
    protected String hashedfileId;
  
    // Class constructor
    protected Message(String version,int senderId,String fileId){
        this.version=version;
        this.senderId=senderId;
        this.hashedfileId=fileId;
    }
    
    // build header function
    protected abstract byte[] buildHeader() throws UnsupportedEncodingException;

    // send message fucntion
    protected abstract void send();

  


}