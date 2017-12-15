package floorplan;

import javax.json.*;
import javax.swing.JFrame;

public class App {
	static JFrame frame = new JFrame("floorplanner");
	public static void main(String[] args) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1920, 1080);
		
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		
		
		frame.add(new Floorplan(frame));
		frame.setVisible(true);
	}


}
