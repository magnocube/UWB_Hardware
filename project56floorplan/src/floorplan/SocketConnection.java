package floorplan;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.io.*;
import java.net.Socket;

public class SocketConnection {
    Socket socket = null;
    DataOutputStream os ;

    PrintWriter printer;
    public static void main(String args[]) {
        new SocketConnection();
    }
    public void send(String anchor,String tag,double distance)
    {
        printer.println("{\"anchor\":"+anchor+",\"tag\":"+tag+",\"distance\":"+distance+"}");
    }
    public void sendString(String result) {
        printer.write(result);
        printer.flush();

    }
    public void send2(String anchor1, String tag1, double distance1, String anchor2, String tag2, double distance2) {

        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObject value = factory.createObjectBuilder()
                .add("anchor", anchor1)
                .add("tag", tag1)
                .add("distance", ""+distance1)
                .add("anchor2", anchor2)
                .add("tag2", tag2)
                .add("distance2", ""+distance2)


                .build();
       String toSend = value.toString();
        System.out.println(toSend);
        printer.println(toSend);

    }
    public boolean connect() {
        System.out.println("started");
        int port = 8378;
        String ip = "77.172.10.240";
        try {
            socket = new Socket(ip, port);

            socket.setSoTimeout(10000);
            System.out.println("Connected");
            printer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }



}
