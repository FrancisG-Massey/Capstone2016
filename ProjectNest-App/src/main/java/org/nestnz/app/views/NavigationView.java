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
import org.nestnz.app.views.map.TrapPositionLayer;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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
	
	final MapView map = new MapView();
	
	final TrapPositionLayer trapPositionLayer = new TrapPositionLayer();

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
        getStylesheets().add(TraplineListView.class.getResource("styles.css").toExternalForm());
    }
    
    private void initControls () {
    	HBox topBox = new HBox();
    	
        Label distanceLabel = new Label("0.0");
        distanceLabel.setMaxWidth(1000.0);
        distanceLabel.setId("distance-label");
        distanceLabel.setAlignment(Pos.CENTER);
        
        distanceLabel.textProperty().bind(distanceToTrap);
        HBox.setHgrow(distanceLabel, Priority.ALWAYS);
                
        setTop(topBox);
        
        prev.toFront();
        prev.setAlignment(Pos.CENTER);
        
        next.toFront();
        next.setAlignment(Pos.CENTER);
        
        topBox.getChildren().addAll(prev, distanceLabel, next);
        
        
        Button logCatch = new Button();
        logCatch.getStyleClass().add("large-button");
        logCatch.setText("Log Catch");
        logCatch.setOnAction(evt -> {
        	catchSelectDialog.showAndWait();
        });
        setBottom(logCatch);
        

		map.setZoom(17); 
        map.setCenter(new MapPoint(-40.3148, 175.7775));
		map.addLayer(trapPositionLayer);
		setCenter(map);
    	
    	
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
    			trapPositionLayer.setCurrentPosition(newPos);
        	}
        });
    }
    
    private final Dialog<CatchType> makeCatchDialog () {
    	Dialog<CatchType> dialog = new Dialog<>(true);
    	GridPane controls = new GridPane();
    	dialog.setContent(controls);
    	dialog.setTitleText("Select Catch");
    	
    	ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        controls.getColumnConstraints().addAll(column1, column2);
    	
    	Button empty = new Button("Empty");
    	GridPane.setConstraints(empty, 0, 0);//Set as top-left cell
    	GridPane.setHgrow(empty, Priority.ALWAYS);
    	GridPane.setVgrow(empty, Priority.ALWAYS);
    	empty.setOnAction(evt -> {
    		LOG.log(Level.INFO, "Empty.");
    	});
    	
    	CatchType op2 = new CatchType(2, "Option 2", null);
    	Button option2 = makeOptionButton(op2, 1);
    	
    	Button other = new Button("Other");
    	other.getStyleClass().add("large-button");
    	GridPane.setConstraints(other, 0, 1, 2, 1);//Set as center cell (spans both rows)

    	CatchType op3 = new CatchType(3, "Option 3", null);
    	Button option3 = makeOptionButton(op3, 2);

    	CatchType op4 = new CatchType(4, "Option 4", null);
    	Button option4 = makeOptionButton(op4, 3);
    	
    	controls.getChildren().addAll(empty, option2, option3, option4, other);
    	return dialog;
    }
    
    private Button makeOptionButton (CatchType catchType, int place) {
    	Button button = new Button(catchType.getName());
    	button.getStyleClass().add("large-button");
    	GridPane.setConstraints(button, place % 2, place > 1 ? 2 : 0);
    	GridPane.setHgrow(button, Priority.ALWAYS);
    	GridPane.setVgrow(button, Priority.ALWAYS);
    	button.setOnAction(evt -> {
    		LOG.log(Level.INFO, "Selected catch: "+catchType);
    	});
    	return button;
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
    	trapPositionLayer.setActiveTrap(trap);
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
    	trapPositionLayer.getTraps().setAll(trapline.getTraps());
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
