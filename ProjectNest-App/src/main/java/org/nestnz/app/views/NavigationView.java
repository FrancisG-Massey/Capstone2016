package org.nestnz.app.views;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.services.HeadingService;
import org.nestnz.app.services.NestPlatformFactory;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class NavigationView extends View {

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
    private Polygon directionArrow = new Polygon(-1,0,1,0,3,0);
    
    private Position origin = null;
    
    private Label coordStatus = new Label();
    
    private List<CoordTime> coords = new ArrayList<>();
    
    private class CoordTime {
    	private Position coords;
    	private LocalDateTime time;
    	
    	private CoordTime(Position coords, LocalDateTime time) {
    		this.coords = coords;
    		this.time = time;
    	}
    }

    public NavigationView(String name) {
        super(name);
        
        getStylesheets().add(NavigationView.class.getResource("secondary.css").toExternalForm());
        
        Label label = new Label("This is Secondary!");

        VBox controls = new VBox(label);
        controls.setAlignment(Pos.CENTER);
        
        setCenter(controls);
        
        setShowTransitionFactory(BounceInRightTransition::new);
        
        getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
            e -> System.out.println("Info")));
        
        Label headingLabel = new Label("Heading not yet determined");
        
        Label positionLabel = new Label("Your device does not support GPS.");
        
        Label movedLabel = new Label();
        
        Button save = new Button("Save coordinate list");
        
        Button clear = new Button("Clear coordinate list");
        
        save.setOnAction(evt -> saveCoords());
        
        clear.setOnAction(evt -> coords.clear());
        
        Group directionGroup = new Group();
        Scene directionView = new Scene(directionGroup, Color.BLACK);
        directionArrow.setFill(Color.BLACK);
        directionGroup.getChildren().add(directionArrow);
        
        controls.getChildren().addAll(directionGroup, headingLabel, positionLabel, movedLabel, save, clear, coordStatus);
        PlatformFactory.getPlatform().getPositionService().positionProperty().addListener((obs, oldPos, newPos) -> {
        	if (newPos != null) {
        		if (origin == null) {
        			origin = newPos;
        		}
        		LOG.info(String.format("Found coordinates: %f, %f", newPos.getLatitude(), newPos.getLongitude()));
        		
        		coords.add(new CoordTime(newPos, LocalDateTime.now()));
        		positionLabel.setText("Latitude: "+newPos.getLatitude()+", longitude: "+newPos.getLongitude());
        		if (oldPos != null) {
        			movedLabel.setText(String.format("Moved by %1$.2f meters (total of %2$.2f meters from origin)", getDifference(oldPos, newPos), getDifference(origin, newPos)));
        		}
        		save.setText("Save coordinate list ("+coords.size()+")");
        	}
        });
        
        HeadingService headingService = NestPlatformFactory.getPlatform().getHeadingService();
        
        if (headingService.isHeadingAvailable()) {
        	headingService.headingProperty().addListener((obs, oldHeading, newHeading) -> {
            	if (newHeading != null) {
            		headingLabel.setText(String.format("Heading: %f", newHeading));
            	}
            });
        } else {
        	headingLabel.setText("Your device does not support heading!");
        }
    }
    
    private File saveCoords () {
    	try {
	    	File path = PlatformFactory.getPlatform().getPrivateStorage();
	    	File file = new File(path, "coords.txt");
	    	try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
	    		CoordTime prev = null;
	    		for (CoordTime pos : coords) {
	    			writer.print("["+pos.time+"] Latitude: "+pos.coords.getLatitude()+", longitude: "+pos.coords.getLongitude());
	    			if (prev != null) {
	    				writer.print(String.format(", moved by %1$.2f meters", getDifference(prev.coords, pos.coords)));
	    			}
	    			writer.println();
	    			prev = pos;
	    		}
	    	}
    		coordStatus.setText("Saved coords to "+file);
	    	return file;
    	} catch (IOException ex) {
    		coordStatus.setText("Failed to save coords");
    		System.out.println("Failed to save coordinates: "+ex.getMessage());
    		return null;
    	}
    }
    
    
    private double getDifference (Position pos1, Position pos2) {
    	return distance(pos1.getLatitude(), pos2.getLatitude(), pos1.getLongitude(), pos2.getLongitude());
    }
    
    
    /**
     * Calculate distance between two points in latitude and longitude. Uses Haversine method as its base.
     * 
     * Source: http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     * 
     * @param lat1 Start point latitude
     * @param lat2 End point latitude
     * @param lon1 Start point longitude
     * @param lon2 End point longitude
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,double lon2) {
    	return distance(lat1, lat2, lon1, lon2, 0, 0);
    }
    
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * 
     * Source: http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     * 
     * @param lat1 Start point latitude
     * @param lat2 End point latitude
     * @param lon1 Start point longitude
     * @param lon2 End point longitude
     * @param el1 Start altitude in meters
     * @param el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
            double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        appBar.setTitleText("Navigation");
        appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
    }
    
}
