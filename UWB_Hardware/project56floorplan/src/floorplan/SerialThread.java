package floorplan;

/**
 * Created by Rene Schouten on 12/2/2017.
 */
public class SerialThread extends Thread{
    private final Floorplan floorplan;
    String com;
    private boolean kill;

    public SerialThread(String text, Floorplan floorplan)
    {
        com = text;
        this.floorplan=floorplan;
    }
    public void run()
    {
        SocketConnection con=null;
        if(floorplan.sendSerial.isSelected()) {
            Serial serial = new Serial(com);
            con = new SocketConnection();
            con.connect();
        }
        if(floorplan.displaySerial.isSelected()) {
            if (floorplan.triangulation == null) {
                floorplan.triangulation = new Triangulation(floorplan.uwbConfiguration);
            }
        }
        String input="";
        while(!kill)
        {
            String result= Serial.getDataIn();
            if(!result.equals("")) {
                result = result.replace("\n", "");
                input+=result;

                if(input.indexOf("}")!=-1)
                {
                    if(floorplan.sendSerial.isSelected())
                    {
                        con.sendString(input);
                    }
                    System.out.println(input);
                    floorplan.consolePrint(input);
                    if(floorplan.displaySerial.isSelected()) {
                        floorplan.triangulation.calculateFromJson(input);
                    }
                    input="";

                }
            }
            sleep(10);
        }
    }
    public void sleep(int time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        kill=true;
    }
}
