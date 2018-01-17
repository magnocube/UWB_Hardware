package floorplan;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

/**
 * Created by Rene Schouten on 12/14/2017.
 */
public class Whatsapp {
    public static void main(String args[])
    {
        Robot r=null;
        try {
            r=new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        while(true)
        {
            String text = "timo fuck you";
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            r.keyPress(KeyEvent.VK_CONTROL);
            r.keyPress(KeyEvent.VK_V);
            r.keyRelease(KeyEvent.VK_V);
            r.keyRelease(KeyEvent.VK_CONTROL);

            sleep(10);
            r.keyPress(KeyEvent.VK_ENTER);
            r.keyRelease(KeyEvent.VK_ENTER);
            sleep(500);

        }
    }
    public static void sleep(int time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
