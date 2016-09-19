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

import static org.nestnz.app.util.NavigationTools.getDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.CompassService;
import org.nestnz.app.services.NestPlatformFactory;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import eu.hansolo.fx.AirCompass;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class NavigationView extends View {
	
	public static final String NAME = "navigation";

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
    final Button prev = MaterialDesignIcon.ARROW_BACK.button(evt -> previousTrap());;
    
    final Button next = MaterialDesignIcon.ARROW_FORWARD.button(evt -> nextTrap());
    
    final List<Trap> orderedTraps = new ArrayList<>();
    
    int currentPointer = 0;
    
    final ObjectProperty<Trap> trapProperty = new SimpleObjectProperty<>();
    
    final ObjectProperty<Position> targetCoordsProperty = new SimpleObjectProperty<>();
    
    final StringProperty distanceToTrap = new SimpleStringProperty();
    
    final Dialog<CatchType> catchSelectDialog;

    public NavigationView() {
        this(false);
    }
    
    protected NavigationView(boolean test) {
    	super(NAME);        
        //setShowTransitionFactory(BounceInRightTransition::new);
        
        //getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
        //    e -> System.out.println("Info")));
        
        initControls();
        if (!test) {
        	initMonitors();
            catchSelectDialog = makeCatchDialog();
        } else {
        	catchSelectDialog = null;
        }
    }
    
    private void initControls () {        
        Label distanceLabel = new Label("0.0");
        distanceLabel.setMaxWidth(1000.0);
        distanceLabel.setId("distance-label");
        distanceLabel.setAlignment(Pos.CENTER);
        
        distanceLabel.textProperty().bind(distanceToTrap);
                
        setTop(distanceLabel);
        
        prev.toFront();
        prev.setAlignment(Pos.CENTER);
        
        next.toFront();
        next.setAlignment(Pos.CENTER);
        
        setLeft(prev);
        setRight(next);
        
        Button logCatch = new Button();
        logCatch.setMaxWidth(1000);
        logCatch.setText("Log Catch");
        logCatch.setOnAction(evt -> {
        	catchSelectDialog.showAndWait();
        });
        setBottom(logCatch);
    	
    	
    	trapProperty.addListener((obs, oldV, newV) -> {
    		LOG.log(Level.INFO, "Selected trap: "+newV);
    		if (newV == null) {
    			targetCoordsProperty.set(null);
    		} else {
    			targetCoordsProperty.set(new Position(newV.getLatitude(), newV.getLongitude()));
    			prev.setVisible(hasPreviousTrap());
    			next.setVisible(hasNextTrap());
    		}
    	});
    }
    
    private void initMonitors () {    	
    	PlatformFactory.getPlatform().getPositionService().positionProperty().addListener((obs, oldPos, newPos) -> {
        	if (newPos != null && targetCoordsProperty.get() != null) {
        		double distance = getDistance(newPos, targetCoordsProperty.get());
        		LOG.info(String.format("Found coordinates: %1$.3f, %2$.3f (distance=%3$.2fm)", newPos.getLatitude(), newPos.getLongitude(), distance));
        		distanceToTrap.set(String.format("%1$.0fm", distance));
        	}
        });
        
        CompassService headingService = NestPlatformFactory.getPlatform().getCompassService();
        
        if (headingService.isHeadingAvailable()) {
        	AirCompass compass = new AirCompass();
        	headingService.headingProperty().addListener((obs, oldHeading, newHeading) -> {
            	if (newHeading != null) {
            		compass.setBearing(newHeading.doubleValue());
            	}
            });
        	//setCenter(compass);
        }
    }
    
    private final Dialog<CatchType> makeCatchDialog () {
    	Dialog<CatchType> dialog = new Dialog<>(true);
    	BorderPane controls = new BorderPane();
    	dialog.setContent(controls);
    	
    	Button empty = new Button("Empty");
    	empty.setMaxWidth(1000);
    	empty.setMaxHeight(1000);
    	empty.setOnAction(evt -> {
    		LOG.log(Level.INFO, "Empty.");
    	});
    	
    	Button option2 = new Button();
    	option2.setMaxWidth(1000);

    	Button option3 = new Button();
    	Button option4 = new Button();
    	Button other = new Button();
    	BorderPane.setAlignment(empty, Pos.TOP_LEFT);
    	BorderPane.setAlignment(option2, Pos.TOP_RIGHT);
    	BorderPane.setAlignment(option3, Pos.BOTTOM_LEFT);
    	BorderPane.setAlignment(option4, Pos.BOTTOM_RIGHT);
    	BorderPane.setAlignment(other, Pos.CENTER);
    	controls.getChildren().addAll(empty, option2, option3, option4, other);
    	return dialog;
    }
    
    /**
     * Go back to the previous trap in the trapline
     */
    public void previousTrap () {
		LOG.log(Level.FINE, "Requested swap to previous trap");
		currentPointer--;
		setTrap(orderedTraps.get(currentPointer));
    }
    
    /**
     * Jump to the next trap in the trapline
     */
    public void nextTrap() {
		LOG.log(Level.FINE, "Requested swap to next trap");
		currentPointer++;
    	setTrap(orderedTraps.get(currentPointer));
    }
    
    /**
     * Checks whether a trap exists prior to the selected trap
     * @return True if a previous trap exists, false if this is the first trap in the line
     */
    public boolean hasPreviousTrap () {
    	return currentPointer > 0;
    }
    
    /**
     * Checks whether another trap exists after the current one
     * @return True if a next trap exists, false if this is the last trap in the trapline
     */
    public boolean hasNextTrap () {
    	return currentPointer < orderedTraps.size()-1;
    }
    
    /**
     * Sets the target trap for the navigation view
     * @param trap The target trap
     */
    void setTrap (Trap trap) {
    	trapProperty.set(trap);
    }
    
    /**
     * Sets the trapline for the navigation view & sets the first trap as trap #1 (or whichever is the lowest number)
     * @param trapline
     */
    public final void setTrapline (Trapline trapline) {
    	Objects.requireNonNull(trapline);
    	
    	//Since Android/iOS don't support Java 8 streams, we have to do it the old way (adding the elements to another list & using Collections.sort()).
    	orderedTraps.clear();
    	orderedTraps.addAll(trapline.getTraps());
    	Collections.sort(orderedTraps, (t1, t2) -> {
    		return t1.getNumber() - t2.getNumber();
    	});
    	
    	setTrap(orderedTraps.get(0));
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        trapProperty.addListener((obs, oldV, newV) -> {
        	if (newV != null) {
                appBar.setTitleText("Trap "+newV.getNumber());        		
        	}
        });
        appBar.setTitleText("Trap "+trapProperty.get().getNumber());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> MobileApplication.getInstance().switchToPreviousView()));
    }
    
}
