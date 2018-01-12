package floorplan;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonCreator {
	private ArrayList<Anchor> anchors;
	private ArrayList<Room> rooms;
	private UWBConfiguration uwbConfiguration;
	private String latestCalculatedJson = " ";
	private UWBUtils uwbUtils;
	public JsonCreator(UWBConfiguration uwbConfiguration, UWBUtils uwbUtils) {
		this.rooms = uwbConfiguration.rooms;
		this.anchors = uwbConfiguration.anchors;
		this.uwbUtils=uwbUtils;
		this.uwbConfiguration=uwbConfiguration;
		try {
			generateJson(uwbConfiguration.scale);
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
						.add("X", uwbUtils.scaledPixelsToRealPixels(anchor.getLocation().getX()))
						.add("Y", uwbUtils.scaledPixelsToRealPixels(anchor.getLocation().getY()))
						.add("master", anchor.isMaster())
						.add("fake", anchor.isFake());
				anchorObject.add(anchorArray);
		}

		j.add("anchors",anchorObject);

		j.add("scale",(double)Math.round(uwbUtils.scaledPixelsToRealPixels(scale)*10000)/10000);
        j.add("imageName",uwbConfiguration.imageName);


		JsonArrayBuilder roomObject = factory.createArrayBuilder();
		JsonObjectBuilder sRoom;
		if(rooms.size() != 0) {
				for(Room i : rooms) {
					sRoom = factory.createObjectBuilder()
					.add("name", i.getName()).add("x1",uwbUtils.scaledPixelsToRealPixels( (int)i.getLocation1().getX())).add("y1", uwbUtils.scaledPixelsToRealPixels((int)i.getLocation1().getY())).add("x2", uwbUtils.scaledPixelsToRealPixels((int)i.getLocation2().getX())).add("y2",uwbUtils.scaledPixelsToRealPixels((int)i.getLocation2().getY()));
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
