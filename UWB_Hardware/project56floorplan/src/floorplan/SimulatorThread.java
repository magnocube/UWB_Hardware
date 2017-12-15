package floorplan;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by Rene Schouten on 11/29/2017.
 */
public class SimulatorThread extends Thread{


    boolean upPressed=false;
    boolean downPressed=false;
    boolean leftPressed=false;
    boolean rightPressed=false;
    Simulator simulator;
    Point location;
    SocketConnection con = new SocketConnection();
    public SimulatorThread(Simulator simulator,Point location) {
        this.simulator=simulator;
        this.location=location;

    }

    public void run()
    {
        int counter=0;
        while(simulator.isOn())
        {

            simulator.floorplan.requestFocusInWindow();
            simulator.floorplan.setFocusable(true);
            if(rightPressed)
            {
                location.setLocation(location.getX()+15,location.getY());
            }
            if(leftPressed)
            {
                location.setLocation(location.getX()-15,location.getY());
            }
            if(upPressed)
            {
                location.setLocation(location.getX(),location.getY()-15);
            }
            if(downPressed)
            {
                location.setLocation(location.getX(),location.getY()+15);
            }
            simulator.floorplan.updateGui();
            ArrayList<Anchor> anchors = simulator.floorplan.uwbConfiguration.anchors;
            ArrayList<RangingData> rangingData = new ArrayList<>();
            double metersDistance[]=new double[anchors.size()];
            for (int i = 0; i < anchors.size(); i++) {
                if(!anchors.get(i).isFake()) {
                    double x1 = anchors.get(i).getLocation().getX();
                    double x2 = location.getX();
                    double y1 = anchors.get(i).getLocation().getY();
                    double y2 = location.getY();
                    double pixeldistance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
                    metersDistance[i] = simulator.floorplan.pixelsToMeters(pixeldistance);

                    simulator.floorplan.triangulation.dataToTag(anchors.get(i).getName(), "AAA1", metersDistance[i]);
                }
            }
            if(counter%5==0)
            {
                con.send2(anchors.get(0).getName(),"AAA1",metersDistance[0],anchors.get(1).getName(),"AAA1",metersDistance[1]);
            }
            counter++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key==KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (key==KeyEvent.VK_LEFT){
            leftPressed = true;
        }
        if (key==KeyEvent.VK_RIGHT){
            rightPressed = true;
        }
        if (key==KeyEvent.VK_DOWN){
            downPressed = true;
        }

    }

    public void keyReleased(KeyEvent e) {
        int key=e.getKeyCode();
        if (key==KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (key==KeyEvent.VK_LEFT){
            leftPressed = false;
        }
        if (key==KeyEvent.VK_RIGHT){
            rightPressed = false;
        }
        if (key==KeyEvent.VK_DOWN){
            downPressed = false;
        }
    }
}
