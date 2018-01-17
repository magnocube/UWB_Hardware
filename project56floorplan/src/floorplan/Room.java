package floorplan;

import java.awt.Point;

public class Room {
	private String name="";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Point getLocation1() {
		return location;
	}
	public Point getLocation2() {
		return location2;
	}
	public void setLocation1(Point location) {
		this.location = location;
	}
	public void setLocation2(Point location) {
		this.location2 = location;
	}
	public void setIsMoving1(boolean isMoving) {
		this.isMoving1=isMoving;
	}
	public void setIsMoving2(boolean isMoving) {
		this.isMoving2=isMoving;
	}
	public boolean isMoving1() {
		return isMoving1;
	}
	public boolean isMoving2() {
		return isMoving2;
	}
	
	private Point location;
	private Point location2;
	private boolean selected=false;
	private boolean isMoving1=false;
	private boolean isMoving2=false;
	
	public Room(String _name, Point _location,Point _location2, boolean _isMoving1, boolean _isMoving2)
	{
		name=_name;
		location=_location;
		location2=_location2;
		isMoving1=_isMoving1;
		isMoving2=_isMoving2;
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
	
}
