package floorplan;

import java.io.*;
import java.net.Socket;

/**
 * Created by Rene Schouten on 12/15/2017.
 */
public class ConfigConnection {
    public final static int SOCKET_PORT = 8377;
    public final static String SERVER = "77.172.10.240";
    public void sendConfig(String json) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Socket sock = null;
        try {
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");
            os = sock.getOutputStream();
            DataOutputStream out = new DataOutputStream(os);

            out.writeUTF("{\"command\":\"setConfiguration\"}\"");
            out.writeUTF(json);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendImage(String filePath) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Socket sock = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");
            os = sock.getOutputStream();
            DataOutputStream out = new DataOutputStream(os);

            out.writeUTF("{\"command\":\"setImageFast\", \"name\":\"room1\"}\"");

            File myFile = new File (filePath);
            byte [] mybytearray  = new byte [(int)myFile.length()];
            fis = new FileInputStream(myFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);

            System.out.println("Sending " + filePath + "(" + mybytearray.length + " bytes)");
            os.write(mybytearray,0,mybytearray.length);
            os.flush();
            System.out.println("Done.");
            os.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
