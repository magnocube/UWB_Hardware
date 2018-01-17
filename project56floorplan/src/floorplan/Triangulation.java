package floorplan;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.awt.*;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Rene Schouten on 12/2/2017.
 */
public class Triangulation {
    private double scale;
    public ArrayList<Tag> tags = new ArrayList<>();
    public UWBConfiguration uwbConfiguration;
    public Triangulation(UWBConfiguration uwbData) {
        this.uwbConfiguration = uwbData;
        scale=uwbData.scale;
    }
    public void calculate(Tag tag) {
        tag.clear();
        if(tag.rangeData.size()<2) {
            return;//not enough data
        }
        int loopCounter1=0;
        int loopCounter2=0;
        for(RangingData rangingData : tag.rangeData)
        {
            for(RangingData rangingData2 : tag.rangeData)
            {
                if(loopCounter1<loopCounter2)
                {
                    Anchor anchor2= rangingData.getAnchor(uwbConfiguration.anchors);
                    Anchor anchor1= rangingData2.getAnchor(uwbConfiguration.anchors);

                    System.out.println("anchor1:"+ rangingData.getAnchorName()+" anchor2:"+ rangingData2.getAnchorName());
                    double aDeltaX=Math.abs(anchor1.getLocation().getX()-anchor2.getLocation().getX());
                    double aDeltaY=Math.abs(anchor1.getLocation().getY()-anchor2.getLocation().getY());
                    double a=Math.sqrt(Math.pow(aDeltaX,2)+Math.pow(aDeltaY,2));
                    double b=metersToPixels(rangingData.getDistance());
                    double c=metersToPixels(rangingData2.getDistance());
                    System.out.println("a: "+a+" b: "+b+" c: "+c);


                    double cAngle= 2*Math.PI - Math.acos((b*b+a*a-c*c)/(2*a*b));
                    System.out.println("cangle"+cAngle);
                    System.out.println("alfa:"+Math.toDegrees(cAngle));
                    double d= Math.cos(cAngle)*b;
                    double e=Math.sin(cAngle)*b;
                    System.out.println("d: "+d+" e: "+e);
                    double anchorsAngle=Math.atan2(aDeltaY,aDeltaX);
                    double distanceFrom0 = Math.sqrt(d*d+e*e);

                    double tagAngleFrom0=Math.atan2(d,e);
                    double destinationtagAngle=0;
                    if(anchor2.getLocation().getY()>anchor1.getLocation().getY()) {
                        if(anchor2.getLocation().getX()>anchor1.getLocation().getX()) {
                            destinationtagAngle = tagAngleFrom0 + (Math.PI-anchorsAngle);
                        }
                        else {
                            destinationtagAngle = tagAngleFrom0 + anchorsAngle;
                        }
                    }
                    else {
                        if(anchor2.getLocation().getX()>anchor1.getLocation().getX()) {
                            destinationtagAngle = tagAngleFrom0 - (Math.PI-anchorsAngle);
                        }
                        else {
                            destinationtagAngle = tagAngleFrom0 - anchorsAngle;
                        }
                    }
                    double tagx= Math.sin(destinationtagAngle)*distanceFrom0+anchor2.getLocation().getX();
                    double tagy= Math.cos(destinationtagAngle)*distanceFrom0+anchor2.getLocation().getY();

                    System.out.println("angle:"+anchorsAngle+" distance from 0 "+distanceFrom0+" angle:"+Math.toDegrees(cAngle)  +" destination angle:"+Math.toDegrees(destinationtagAngle));
                    tag.setPotentialLocation(new Point((int)(tagx),(int)(tagy)));




                    cAngle= Math.acos((b*b+a*a-c*c)/(2*a*b));
                    System.out.println("alfa:"+Math.toDegrees(cAngle));
                    d= Math.cos(cAngle)*b;
                    e=Math.sin(cAngle)*b;
                    System.out.println("d: "+d+" e: "+e);
                    anchorsAngle=Math.atan2(aDeltaY,aDeltaX);
                    distanceFrom0 = Math.sqrt(d*d+e*e);

                    tagAngleFrom0=Math.atan2(d,e);
                    if(anchor2.getLocation().getY()>anchor1.getLocation().getY()) {
                        if(anchor2.getLocation().getX()>anchor1.getLocation().getX()) {
                            destinationtagAngle = tagAngleFrom0 + (Math.PI-anchorsAngle);
                        }
                        else {
                            destinationtagAngle = tagAngleFrom0 + anchorsAngle;
                        }
                    }
                    else {
                        if(anchor2.getLocation().getX()>anchor1.getLocation().getX()) {
                            destinationtagAngle = tagAngleFrom0 - (Math.PI-anchorsAngle);
                        }
                        else {
                            destinationtagAngle = tagAngleFrom0 - anchorsAngle;
                        }
                    }
                    tagx= Math.sin(destinationtagAngle)*distanceFrom0+anchor2.getLocation().getX();
                    tagy= Math.cos(destinationtagAngle)*distanceFrom0+anchor2.getLocation().getY();

                    System.out.println("angle:"+anchorsAngle+" distance from 0 "+distanceFrom0+" angle:"+Math.toDegrees(cAngle)  +" destination angle:"+Math.toDegrees(destinationtagAngle));
                    tag.setPotentialLocation(new Point((int)(tagx),(int)(tagy)));
                }
                loopCounter2++;
            }
            loopCounter2=0;
            loopCounter1++;
        }
        tag.calculate();
    }

    public void calculateFromJson(String input) {
        JsonReader jsonReader = Json.createReader(new StringReader(input));
        JsonObject json = jsonReader.readObject();
        jsonReader.close();


        String aName = json.getString("anchor").toString().replaceAll("\"", "");
        String tName = json.getString("tag").toString().replaceAll("\"", "");
        double distance = Double.parseDouble(json.getString("distance").toString().replaceAll("\"", ""));
        String aName2 = json.getString("anchor2").toString().replaceAll("\"", "");
        String tName2 = json.getString("tag2").toString().replaceAll("\"", "");
        double distance2 = Double.parseDouble(json.getString("distance2").toString().replaceAll("\"", ""));

        dataToTag(aName,tName,distance);
        dataToTag(aName2,tName2,distance2);

        /*ArrayList<RangingData> rangingData = new ArrayList<>();
        rangingData.add(new RangingData(aName, tName, distance));
        rangingData.add(new RangingData(aName2, tName2, distance2));
        calculate(rangingData);*/
    }

    public void dataToTag(String aName, String tName, double distance) {
        Tag distantTag=null;
        for(int i=0;i<tags.size();i++)
        {
            if(tags.get(i).adress==tName)
            {
                distantTag=tags.get(i);
            }
        }
        if(distantTag==null)
        {
            distantTag=new Tag(uwbConfiguration,tName);
            tags.add(distantTag);
        }
        distantTag.addDistanceData(aName,distance);
        calculate(distantTag);
    }
    public void cleanTags()
    {
        for(int i=0;i<tags.size();i++)
        {
            if(System.currentTimeMillis()-tags.get(i).lastUpdate>1000)
            {
                tags.remove(i);
            }
        }
    }


    public double metersToPixels(double meters)
    {
        return meters*scale;
    }
}
