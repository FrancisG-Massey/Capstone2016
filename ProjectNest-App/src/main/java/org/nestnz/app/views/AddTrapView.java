package org.nestnz.app.views;

import static org.nestnz.app.util.NavigationTools.getDistance;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.views.map.PositionLayer;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AddTrapView extends View {

    private static final Logger LOG = Logger.getLogger(AddTrapView.class.getName());
    
	public static final String NAME = "add_trap";
	
	private final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
	
	protected final Button addTrapButton = new Button("Add Trap");
	
	protected final MapView map = new MapView();
	
	protected final PositionLayer mapPositionLayer = new PositionLayer();
	
	protected final Label currentCoords = new Label("Waiting for coordinates...");
	
	private Position lastTrapPosition;
	
	/**
	 * Represents the current position of the device. Updates automatically when new location coordinates are received
	 */
	private final ObjectProperty<Position> currentPosition = new SimpleObjectProperty<>();
	
	/**
	 * The icon indicating the user's current position on the map
	 */
	private final Node curPosIcon = new Circle(10, Color.YELLOW);
	
	private IntegerProperty nextTrapNumber = new SimpleIntegerProperty();
	
	public AddTrapView() {
		super(NAME);
		addTrapButton.setMaxHeight(1000.0);
		addTrapButton.setMaxWidth(1000.0);
		map.setZoom(17); 
        map.setCenter(new MapPoint(-40.3148, 175.7775));
		map.addLayer(mapPositionLayer);

		initPositionMonitor();
		setCenter(map);
		setBottom(addTrapButton);
		
		addTrapButton.setOnAction(evt -> {
			if (currentPosition.get() == null) {
				this.getApplication().showMessage("We haven't figured out your location yet! Please wait a few seconds and try again.");
			} else {
				lastTrapPosition = currentPosition.get();
				addTrap(lastTrapPosition);
				this.getApplication().showMessage(
						String.format("Created trap at %1$.6f, %2$.6f", lastTrapPosition.getLatitude(), lastTrapPosition.getLongitude()));
			}
		});
	}
	
	private void addTrap (Position position) {
		int number = nextTrapNumber.get();
		Trap trap = new Trap(number, null, position.getLatitude(), position.getLongitude());
		nextTrapNumber.set(number+1);
		traplineProperty.get().getTraps().add(trap);
		((NestApplication)this.getApplication()).saveNewTrap(traplineProperty.get().getName(), trap);
		
		Node icon = new Circle(7, Color.RED);
		mapPositionLayer.addPoint(new MapPoint(position.getLatitude(), position.getLongitude()), icon);		
	}
	
	private void addExistingTraps () {
		for (Trap trap : traplineProperty.get().getTraps()) {
			Node icon = new Circle(7, Color.BLUE);
			mapPositionLayer.addPoint(new MapPoint(trap.getLatitude(), trap.getLongitude()), icon);
		}
	}
	
	private void initPositionMonitor () {
		currentPosition.bind(PlatformFactory.getPlatform().getPositionService().positionProperty());
		currentPosition.addListener((obs, oldPos, newPos) -> {
			if (newPos != null) {
				LOG.log(Level.INFO, String.format("Found coords: %1$.6f, %2$.6f", newPos.getLatitude(), newPos.getLongitude()));
				MapPoint curPoint = new MapPoint(newPos.getLatitude(), newPos.getLongitude());
				map.setCenter(curPoint);
				mapPositionLayer.removePoint(curPosIcon);
				mapPositionLayer.addPoint(curPoint, curPosIcon);
        		if (lastTrapPosition == null) {
        			currentCoords.setText(String.format("Latitude: %1$.6f\nLongitude: %2$.6f", newPos.getLatitude(), newPos.getLongitude()));
        		} else {
            		double distance = getDistance(newPos, lastTrapPosition);
        			currentCoords.setText(String.format("Latitude: %1$.6f\nLongitude: %2$.6f\nTravelled %3$.0f meters from last trap", newPos.getLatitude(), newPos.getLongitude(), distance));
        		}
        	}
		});
	}
	
	public void setTrapline (Trapline trapline) {
		traplineProperty.set(trapline);
		int nextTrapNumber = 0;
		for (Trap trap : trapline.getTraps()) {
			if (trap.getNumber() > nextTrapNumber) {
				nextTrapNumber = trap.getNumber();
			}
		}
		this.nextTrapNumber.set(nextTrapNumber+1);
		addExistingTraps();
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> LOG.log(Level.INFO, "Open menu pressed...")));
		appBar.setTitleText("Add Trap "+nextTrapNumber.intValue());
		nextTrapNumber.addListener((obs, oldV, newV) -> {
        	if (newV != null) {
                appBar.setTitleText("Add Trap "+newV);        		
        	}
        });
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
    }	
}
