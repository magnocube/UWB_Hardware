package floorplan;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Rene Schouten on 12/12/2017.
 */
public class UWBConfiguration {
    public ArrayList<Anchor> anchors = new ArrayList<Anchor>();
    public ArrayList<Room> rooms = new ArrayList<Room>();
    public String imageName;
    public double scale;
    public double imageScale;//configurater only
    public void loadFromFile(String path,Floorplan floorplan) {
        anchors.clear();
        rooms.clear();
        scale=0;
        String everything;
        try {

            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
            br.close();
            System.out.println(everything);


            JsonReader jsonReader = Json.createReader(new StringReader(everything));
            JsonObject json = jsonReader.readObject();
            jsonReader.close();
            scale = Double.parseDouble(json.get("scale").toString().replaceAll("\"", ""));
            imageName = json.get("imageName").toString().replaceAll("\"", "");
            floorplan.loadImage(floorplan.fileLocation+imageName);
            floorplan.path.setText(floorplan.fileLocation+imageName);
            for(int i=0;i<json.getJsonArray("anchors").size();i++)
            {
                String anchorName = json.getJsonArray("anchors").getJsonObject(i).get("name").toString().replaceAll("\"", "");
                double anchorX = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("anchors").getJsonObject(i).get("X").toString().replaceAll("\"", "")));
                double anchorY = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("anchors").getJsonObject(i).get("Y").toString().replaceAll("\"", "")));
                boolean fake;
                if(json.getJsonArray("anchors").getJsonObject(i).get("fake").toString().equals("true"))
                {
                    fake=true;
                }
                else{
                    fake=false;
                }
                boolean master;
                if(json.getJsonArray("anchors").getJsonObject(i).get("master").toString().equals("true"))
                {
                    master=true;
                }
                else{
                    master=false;
                }
                anchors.add(new Anchor(anchorName,new Point((int)anchorX,(int)anchorY),master,fake,false,"0"));
            }


            for(int i = 0; i< json.getJsonArray("rooms").size();i++) {
                String name = json.getJsonArray("rooms").getJsonObject(i).get("name").toString();
                double x1 = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("rooms").getJsonObject(i).get("x1").toString().replaceAll("\"", "")));
                double y1 = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("rooms").getJsonObject(i).get("y1").toString().replaceAll("\"", "")));
                double x2 = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("rooms").getJsonObject(i).get("x2").toString().replaceAll("\"", "")));
                double y2 = realPixelsToScaledPixels(Double.parseDouble(json.getJsonArray("rooms").getJsonObject(i).get("y2").toString().replaceAll("\"", "")));
                System.out.println(name + " " + x1 + " " + y1 + " " + x2 + " " + y2 );
                rooms.add(new Room(name,new Point((int)x1,(int)y1),new Point((int)x2,(int)y2),false,false));
            }

        } catch(Exception e) {System.out.println("cant read room locations"); e.printStackTrace(); }



    }
    public double realPixelsToScaledPixels(double pixels)
    {
        return pixels*imageScale;
    }

}
