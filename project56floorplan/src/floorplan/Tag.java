package floorplan;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Rene Schouten on 12/4/2017.
 */
public class Tag {
    private final ArrayList<Anchor> anchors;
    private Point location;
    public ArrayList<Point> potentialLocations = new ArrayList<>();
    private ArrayList<Integer> locationScore = new ArrayList<>();
    public String adress;
    public long lastUpdate=0;
    public ArrayList<RangingData> rangeData = new ArrayList<>();
    public Tag(UWBConfiguration uwbConfiguration,String adress) {
        this.anchors=uwbConfiguration.anchors;
        this.adress=adress;
    }

    public Point getLocation() {
        return location;
    }

    public void setPotentialLocation(Point potentialLocation) {
        potentialLocations.add(potentialLocation);
        locationScore.add(0);
    }

    public void calculate()
    {
        lastUpdate=System.currentTimeMillis();
        for(int i = 0; i< potentialLocations.size(); i++)
        {
            for(int i2 = 0; i2< potentialLocations.size(); i2++)
            {
                if(i>i2)
                {
                    double distance=distanceBetweenPoints(potentialLocations.get(i),potentialLocations.get(i2));
                    System.out.println("i:"+i+" i2: "+i2+" distance: "+distance);
                    if(distance<50) {
                        locationScore.set(i, new Integer(10));
                        locationScore.set(i2, new Integer(10));
                    }

                }
            }
            for(Anchor anchor:anchors)
            {
                if (anchor.isFake()) {
                    double distance=distanceBetweenPoints(potentialLocations.get(i),anchor.getLocation());
                    locationScore.set(i,locationScore.get(i)+(int)(1000-distance)/100);
                }
            }
        }

        int highest=0;
        int highestIndex=0;
        for(int i = 0; i< potentialLocations.size(); i++)
        {
            int score=locationScore.get(i);
            System.out.println(i+"  "+score);
            if(score>highest)
            {
                highest=score;
                highestIndex=i;
            }
        }
        location= potentialLocations.get(highestIndex);

    }
    public void clear() {
        potentialLocations.clear();
        locationScore.clear();
        for(int i=0;i<rangeData.size();i++)
        {
            if(System.currentTimeMillis()-rangeData.get(i).getCreationTime()>500)
            {
                rangeData.remove(i);
            }
            else if((System.currentTimeMillis()-rangeData.get(i).getCreationTime()>300)&&(rangeData.get(i).getDistance()>15))
            {
                rangeData.remove(i);
            }
        }
    }
    private double distanceBetweenPoints(Point a,Point b)
    {
        return Math.sqrt(Math.pow(a.getX()-b.getX(),2)+Math.pow(a.getY()-b.getY(),2));
    }

    public void addDistanceData(String aName, double distance) {
        boolean replaced=false;
        for(RangingData rangingData:rangeData)
        {
            if(rangingData.getAnchorName()==aName)
            {
                rangingData=new RangingData(aName,distance);
                replaced=true;
            }
        }
        if(!replaced) {
            rangeData.add(new RangingData(aName, distance));
        }

    }
}