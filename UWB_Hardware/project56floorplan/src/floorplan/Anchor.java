package floorplan;

import javax.swing.*;
import java.awt.Point;

public class Anchor {

	private String name="";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Point getLocation() {
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	public boolean isMaster() {
		return master;
	}
	public void setIsMoving(boolean isMoving) {
		this.isMoving=isMoving;
	}
	public boolean isMoving() {
		return isMoving;
	}
	public void setMaster(boolean master) {
		this.master = master;
	}
	public boolean isFake() {
		return fake;
	}
	private Point location;
	private boolean master;
	private boolean fake;
	private boolean selected=false;
	private boolean isMoving=false;;
	private String distanceString;
	public Anchor(String _name, Point _location, boolean _master, boolean _fake, boolean _isMoving,  String distance)
	{
		fake=_fake;
		name=_name;
		location=_location;
		master=_master;
		isMoving=_isMoving;
		distanceString=distance;
	}
	public void setSelected() {
		selected=true;
		
	}
	public boolean isSelected() {
		return selected;
	}
	public void deselect() {
		selected=false;
		
	}

	public String getDistanceString() {
		return distanceString;
	}

	public double getDistance()
	{
		double value=0;
		try{
			value = Double.parseDouble(distanceString);
		}
		catch(Exception e)
		{
			return 0;
		}
		return value;

	}

	public void setDistance(String distance) {
		this.distanceString = distance;
	}

}
