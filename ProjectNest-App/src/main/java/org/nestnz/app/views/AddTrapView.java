package org.nestnz.app.views;

import static org.nestnz.app.util.NavigationTools.getDistance;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Trapline;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AddTrapView extends View {

    private static final Logger LOG = Logger.getLogger(AddTrapView.class.getName());
    
	public static final String NAME = "add_trap";
	
	private final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
	
	protected final Button addTrapButton = new Button("Add Trap");
	
	protected final Label currentCoords = new Label("Waiting for coordinates...");
	
	private Position lastTrapPosition;
	
	private final ObjectProperty<Position> lastPos = new SimpleObjectProperty<>();
	
	public AddTrapView() {
		super(NAME);
		addTrapButton.setMaxHeight(1000.0);
		addTrapButton.setMaxWidth(1000.0);
		setCenter(currentCoords);
		setBottom(addTrapButton);
		initPositionMonitor();
		addTrapButton.setOnAction(evt -> {
			if (lastPos.get() == null) {
				this.getApplication().showMessage("We haven't figured out your location yet! Please wait a few seconds and try again.");
			} else {
				lastTrapPosition = lastPos.get();
				this.getApplication().showMessage(
						String.format("Created trap at %1$.6f, %2$.6f", lastTrapPosition.getLatitude(), lastTrapPosition.getLongitude()));
			}
		});
	}
	
	private void initPositionMonitor () {
		PlatformFactory.getPlatform().getPositionService().positionProperty().addListener((obs, oldPos, newPos) -> {
        	if (newPos != null) {
        		if (lastTrapPosition == null) {
        			currentCoords.setText(String.format("Latitude: %1$.6f\nLongitude: %2$.6f", newPos.getLatitude(), newPos.getLongitude()));
        		} else {
            		double distance = getDistance(newPos, lastTrapPosition);
        			currentCoords.setText(String.format("Latitude: %1$.6f\nLongitude: %2$.6f\nTravelled %3$.0f meters from last trap", newPos.getLatitude(), newPos.getLongitude(), distance));
        		}
        	}
        });
		lastPos.bind(PlatformFactory.getPlatform().getPositionService().positionProperty());
	}
	
	public void setTrapline (Trapline trapline) {
		traplineProperty.set(trapline);
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> LOG.log(Level.INFO, "Open menu pressed...")));
		appBar.setTitleText("Add Trap");
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
    }	
}
