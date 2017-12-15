package floorplan;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonCreator {
	private final Floorplan floorplan;
	private ArrayList<Anchor> anchors;
	private ArrayList<Room> rooms;
	private String latestCalculatedJson = " ";
	public JsonCreator(ArrayList<Anchor> anchors, ArrayList<Room> rooms,double scale,Floorplan floorplan) {
		this.rooms = rooms;
		this.anchors = anchors;
	this.floorplan=floorplan;
		try {
			generateJson(scale);
		} catch (Exception e) {
			System.out.println("error in configuration");
		}

	}
	
	
	public void generateJson(double scale) throws Exception {
		
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder j = factory.createObjectBuilder();



		JsonArrayBuilder anchorObject = factory.createArrayBuilder();
		JsonObjectBuilder anchorArray;
			for(Anchor anchor:anchors) {
				String fake="False";
				String master="";

				anchorArray = factory.createObjectBuilder()
						.add("name", anchor.getName())
						.add("X", floorplan.scaledPixelsToRealPixels(anchor.getLocation().getX()))
						.add("Y", floorplan.scaledPixelsToRealPixels(anchor.getLocation().getY()))
						.add("master", anchor.isMaster())
						.add("fake", anchor.isFake());
				anchorObject.add(anchorArray);
		}

		j.add("anchors",anchorObject);

		j.add("scale",(double)Math.round(floorplan.scaledPixelsToRealPixels(scale)*10000)/10000);
        j.add("imageName",floorplan.uwbConfiguration.imageName);


		JsonArrayBuilder roomObject = factory.createArrayBuilder();
		JsonObjectBuilder sRoom;
		if(rooms.size() != 0) {
				for(Room i : rooms) {
					sRoom = factory.createObjectBuilder()
					.add("name", i.getName()).add("x1",floorplan.scaledPixelsToRealPixels( (int)i.getLocation1().getX())).add("y1", floorplan.scaledPixelsToRealPixels((int)i.getLocation1().getY())).add("x2", floorplan.scaledPixelsToRealPixels((int)i.getLocation2().getX())).add("y2", floorplan.scaledPixelsToRealPixels((int)i.getLocation2().getY()));
					roomObject.add(sRoom);
				}
		}
		    
		   j.add("rooms",roomObject);
		   
		  
		JsonObject value = j.build();
		 System.out.println(value.toString());
		 
		 latestCalculatedJson = value.toString();

	}
	
	public String getJson() {
		return latestCalculatedJson;
		
	}
}
