package floorplan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Rene Schouten on 11/29/2017.
 */
public class Simulator{
    public Floorplan floorplan;
    private UWBConfiguration uwbConfiguration;
    private UWBUtils uwbUtils;
    private boolean isOn;
    private Point location;
    private SimulatorThread thread;
    public Simulator(Floorplan floorplan, UWBConfiguration uwbConfiguration,UWBUtils uwbUtils)
    {
        this.uwbConfiguration=uwbConfiguration;
        this.floorplan=floorplan;
        this.uwbUtils=uwbUtils;
    }
    public boolean isOn() {
        return isOn;
    }
    public void startSimulator()
    {
        if (uwbUtils.triangulation == null) {
            uwbUtils.triangulation = new Triangulation(uwbConfiguration);
        }
        isOn=true;
        location=new Point(500,500);
        thread = new SimulatorThread(this,floorplan,uwbConfiguration,uwbUtils,location);

        if(thread.con.connect())
        {
            thread.start();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "error connecting", "error", JOptionPane.INFORMATION_MESSAGE);

        }

    }public Point getLocation()
    {
        return location;
    }

    public void stopSimulator() {
        isOn=false;
    }

    public void keyPressed(KeyEvent e) {
        if(isOn()) {
            thread.keyPressed(e);
        }
    }

    public void keyReleased(KeyEvent e) {
        if(isOn()) {
            thread.keyReleased(e);
        }
    }
}
