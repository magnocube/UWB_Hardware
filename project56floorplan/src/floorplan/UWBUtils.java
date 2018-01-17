package floorplan;

import javax.swing.*;

/**
 * Created by Rene Schouten on 1/12/2018.
 */
public class UWBUtils {
    private UWBConfiguration uwbConfiguration;
    public JTextArea jsonTextField;
    public Triangulation triangulation;
    public UWBUtils(UWBConfiguration uwbConfiguration, JTextArea jsonTextField) {
        this.uwbConfiguration=uwbConfiguration;
        this.jsonTextField = jsonTextField;
    }

    public void calculateScale()
    {
        uwbConfiguration.scale=0;
        int masterIndex=-1;
        for (int i = 0; i < uwbConfiguration.anchors.size(); i++) {
            if (uwbConfiguration.anchors.get(i).isMaster()) {
                masterIndex=i;
            }
        }
        if(masterIndex==-1)
        {
            uwbConfiguration.scale=0;
            return;
        }



        for (int i = 0; i < uwbConfiguration.anchors.size(); i++) {
            if (!uwbConfiguration.anchors.get(i).isMaster()) {
                double distance= uwbConfiguration.anchors.get(i).getDistance();
                if(distance!=0)
                {
                    double x1= uwbConfiguration.anchors.get(i).getLocation().getX();
                    double x2= uwbConfiguration.anchors.get(masterIndex).getLocation().getX();
                    double y1= uwbConfiguration.anchors.get(i).getLocation().getY();
                    double y2= uwbConfiguration.anchors.get(masterIndex).getLocation().getY();
                    double pixeldistance=Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
                    uwbConfiguration.scale=pixeldistance/distance;
                }
            }
        }
        if(uwbConfiguration.scale<=0)
        {
            uwbConfiguration.scale=0;
        }
    }

    public double pixelsToMeters(double pixeldistance) {
        return pixeldistance/ uwbConfiguration.scale;
    }
    public double metersToPixels(double meters)
    {
        return meters* uwbConfiguration.scale;
    }
    public double scaledPixelsToRealPixels(double pixels)
    {
        return pixels/uwbConfiguration.imageScale;
    }
    public void consolePrint(String text)
    {
        String originalText=jsonTextField.getText();
        if(originalText.length()>1000)
        {
            originalText= originalText.substring(0,1000);
        }
        jsonTextField.setText(text+"\n" +originalText);
    }
}
