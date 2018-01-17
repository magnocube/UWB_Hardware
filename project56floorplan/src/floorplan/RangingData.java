package floorplan;

import java.util.ArrayList;

/**
 * Created by Rene Schouten on 12/2/2017.
 */
public class RangingData {
    public String getAnchorName() {
        return anchorName;
    }

    private String anchorName;

    public double getDistance() {
        return distance;
    }
    private double distance;
    public long creationTime;
    public long getCreationTime() {
        return creationTime;
    }
    public RangingData(String anchorName, double distance) {
        this.anchorName=anchorName;
        this.distance=distance;
        creationTime =System.currentTimeMillis();

    }

    public Anchor getAnchor(ArrayList<Anchor> anchors) {
        for(Anchor anchor:anchors)
        {
            if(anchor.getName().equals(anchorName))
            {
                return anchor;
            }
        }
        return null;
    }


}
