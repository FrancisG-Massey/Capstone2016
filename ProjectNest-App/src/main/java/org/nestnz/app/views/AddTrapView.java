/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.views;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.views.map.TrapPositionLayer;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapView;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

public class AddTrapView extends View {

    private static final Logger LOG = Logger.getLogger(AddTrapView.class.getName());
    
	public static final String NAME = "add_trap";
	
	private Trapline trapline;
	
	protected final Button addTrapButton = new Button("Add Trap");
	
	protected final MapView map = new MapView();
	
	protected final TrapPositionLayer trapPositionLayer = new TrapPositionLayer();
	
	private Position lastTrapPosition;
	
	/**
	 * Represents the current position of the device. Updates automatically when new location coordinates are received
	 */
	private final ObjectProperty<Position> currentPosition = new SimpleObjectProperty<>();
	
	private IntegerProperty nextTrapNumber = new SimpleIntegerProperty();
	
	public AddTrapView() {
		super(NAME);
		addTrapButton.getStyleClass().add("large-button");
		addTrapButton.setVisible(false);//Hide the 'add trap' button until we've set the trapline
		map.setZoom(MapLoadingService.ZOOM);		
		
		map.addLayer(trapPositionLayer);

		getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
				e -> System.out.println("Info")));
		
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
        getStylesheets().add(TraplineListView.class.getResource("styles.css").toExternalForm());
	}
	
	private void addTrap (Position position) {
		Objects.requireNonNull(position);
		
		int number = nextTrapNumber.get();
		Trap trap = new Trap(number, position.getLatitude(), position.getLongitude());
		nextTrapNumber.set(number+1);
		trapline.getTraps().add(trap);
		trapPositionLayer.getTraps().add(trap);
		trapPositionLayer.setActiveTrap(trap);
	}
	
	private void initPositionMonitor () {
		currentPosition.bind(PlatformFactory.getPlatform().getPositionService().positionProperty());
		currentPosition.addListener((obs, oldPos, newPos) -> {
			if (newPos != null) {
				LOG.log(Level.INFO, String.format("Found coords: %1$.6f, %2$.6f", newPos.getLatitude(), newPos.getLongitude()));
				trapPositionLayer.setCurrentPosition(newPos);
				//map.setCenter(newPos.getLatitude(), newPos.getLongitude());
        	}
		});
	}
	
	public void setTrapline (Trapline trapline) {
		Objects.requireNonNull(trapline);
		
		this.trapline = trapline;
		addTrapButton.setVisible(true);
		
		
		int nextTrapNumber = 0;
		for (Trap trap : trapline.getTraps()) {
			if (trap.getNumber() > nextTrapNumber) {
				nextTrapNumber = trap.getNumber();
			}
		}
		this.nextTrapNumber.set(nextTrapNumber+1);
		
		trapPositionLayer.getTraps().setAll(trapline.getTraps());
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
