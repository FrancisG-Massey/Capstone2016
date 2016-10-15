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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.views.map.TrapPositionLayer;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.PositionService;
import com.gluonhq.charm.down.plugins.VibrationService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapView;

import android.R.integer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class NavigationView extends View {
	
	private static enum Distance {
		FAR(100, "far"),
		NORMAL(30, null),
		CLOSE(20, "close"),
		CLOSER(10, "closer"),
		CLOSEST(0, "closest");
		
		double lowerBound;
		PseudoClass cssPseudoClass;
		
		Distance (double lowerBound, String cssClass) {
			this.lowerBound = lowerBound;
			if (cssClass != null) {
				cssPseudoClass = PseudoClass.getPseudoClass(cssClass);
			}
		}
		
		static Distance getForDistance (double distance) {
			for (Distance d : values()) {
				if (distance > d.lowerBound) {
					return d;
				}
			}
			return CLOSEST;
		}
	}
	
	public static final String NAME = "navigation";

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
    final Button prev = MaterialDesignIcon.ARROW_BACK.button(evt -> previousTrap());;
    
    final Button next = MaterialDesignIcon.ARROW_FORWARD.button(evt -> nextTrap());
    
    /**
     * Used to quickly look up an index in {@link #orderedTraps} based on a trap number.
     * Should always be initialised to an array the size of the maximum trap number
     */
    int[] trapNumberLookup;
    
    /**
     * The number of the first trap to use in the navigation sequence
     */
    int startTrap;
    
    /**
     * The number of the last trap to use in the navigation sequence
     */
    int endTrap;
    
    /**
     * The number of traps to jump over when {@link #nextTrap()} is called (1=go to next trap, 2=skip next & go to one after, 3=skip next 2, etc)
     * {@link #previousTrap()} uses the negative of this when calculating the previous trap
     */
    int step;
    
    final ObservableList<Trap> orderedTraps;
    
    final IntegerProperty currentTrapIndex = new SimpleIntegerProperty(0);
    
    final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
    
    final ObjectProperty<Trap> trapProperty = new SimpleObjectProperty<>();
    
    final ObjectProperty<Position> targetCoordsProperty = new SimpleObjectProperty<>();
    
    final DoubleProperty distanceToTrap = new SimpleDoubleProperty();
    
    final Dialog<CatchType> catchSelectDialog;
	
	final MapView map = new MapView();
	
	final TrapPositionLayer trapPositionLayer = new TrapPositionLayer();

    public NavigationView() {
        this(false);
    }
    
    protected NavigationView(boolean test) {
    	super(NAME);        
    	orderedTraps = trapPositionLayer.getTraps();
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
    	topBox.setId("top-box");
    	
    	topBox.setAlignment(Pos.CENTER);
    	
        Label distanceLabel = new Label("0.0");
        distanceLabel.setId("distance-label");
        
        distanceLabel.textProperty().bind(Bindings.format("%1.0f m", distanceToTrap));
        HBox.setHgrow(distanceLabel, Priority.ALWAYS);
                
        setTop(topBox);
        
        prev.toFront();
        prev.getStyleClass().add("prev");
        
        next.toFront();
        prev.getStyleClass().add("next");
        
        topBox.getChildren().addAll(prev, distanceLabel, next);        
        
        Button logCatch = new Button();
        logCatch.getStyleClass().add("large-button");
        logCatch.setText("Log Catch");
        logCatch.setOnAction(evt -> {
        	showLogCatchDialog();
        });
        setBottom(logCatch);
        
        
        trapProperty.bind(Bindings.valueAt(orderedTraps, currentTrapIndex));

        trapPositionLayer.activeTrapProperty().bind(trapProperty);

		map.setZoom(MapLoadingService.ZOOM);
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
    	
    	Optional<VibrationService> vibrationService = Services.get(VibrationService.class);
    	
    	distanceToTrap.addListener((obs, oldDist, newDist) -> {
    		Distance oldDistance = Distance.getForDistance(oldDist.doubleValue());
    		Distance newDistance = Distance.getForDistance(newDist.doubleValue());
    		if (oldDistance != newDistance) {
    			distanceLabel.pseudoClassStateChanged(oldDistance.cssPseudoClass, false);
    			distanceLabel.pseudoClassStateChanged(newDistance.cssPseudoClass, true);
    			if (oldDist.doubleValue() > newDist.doubleValue()) {
    				vibrationService.ifPresent(service -> service.vibrate());
    			}
    		}
    	});
    }
    
    private void initMonitors () {
    	Services.get(PositionService.class).ifPresent(gpsService -> {
    		trapPositionLayer.currentPositionProperty().bind(gpsService.positionProperty());
        	
        	distanceToTrap.bind(Bindings.createDoubleBinding(() -> gpsService.getPosition() == null || targetCoordsProperty.get() == null ? 0 :
        				getDistance(gpsService.getPosition(), targetCoordsProperty.get()), 
        					gpsService.positionProperty(), targetCoordsProperty));
    	});
    }
    
    /**
     * Displays a dialog prompting the user to select the catch type for the current trap
     */
    private void showLogCatchDialog () {
		Trap forTrap = trapProperty.get();
    	catchSelectDialog.setTitleText(String.format("Log catch #%d", forTrap.getNumber()));
    	catchSelectDialog.showAndWait().ifPresent(catchType -> {
        	getApplication().showMessage(String.format("Logged %s in trap #%d", 
        			catchType.getName(), forTrap.getNumber())/*, "Change", evt -> {
        		modifyCatch(loggedCatch);
        	}*/);
        	if (catchType != CatchType.EMPTY) {
        		Catch loggedCatch = new Catch(catchType);
        		forTrap.getCatches().add(loggedCatch);
        	}
        	if (hasNextTrap()) {
        		nextTrap();
        	}
    	});
    }
    
    /**
     * Displays the catch type dialog also displayed in {@link #showLogCatchDialog()}, but allows the user to change the catch specified {@link integer} {@code loggedCatch} 
     * @param loggedCatch The previously specified catch to ask the user to change
     */
    protected void modifyCatch (Catch loggedCatch) {
		Trap forTrap = trapProperty.get();
    	catchSelectDialog.setTitleText(String.format("Change catch #%d", forTrap.getNumber()));
    	catchSelectDialog.showAndWait().ifPresent(catchType -> {
    		LOG.log(Level.INFO, "Changed catch to "+catchType);
        	getApplication().showMessage(String.format("Changed catch in trap #%d from %s to %s", 
        			forTrap.getNumber(), loggedCatch.getCatchType().getName(), catchType.getName()), "Change", evt -> {
        		modifyCatch(loggedCatch);
        	});
        	loggedCatch.setCatchType(catchType);
    	});
    }
    
    /**
     * Builds the catch selection dialog, used by {@link #showLogCatchDialog()}. This only ever needs to be called once when the view is created
     * @return The dialog used to select the creature caught in the active trap
     */
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
    	
    	Button empty = makeOptionButton(0);    	
    	Button option2 = makeOptionButton(1);
    	
    	Button other = new Button("Other");
    	other.setMaxSize(1000, 1000);
    	other.getStyleClass().add("large-button");
    	GridPane.setConstraints(other, 0, 1, 2, 1);//Set as center cell (spans both rows)

    	Button option3 = makeOptionButton(2);
    	Button option4 = makeOptionButton(3);
    	
    	controls.getChildren().addAll(empty, option2, option3, option4, other);
    	return dialog;
    }
    
    private Button makeOptionButton (int place) {
    	ObjectBinding<CatchType> catchType;
    	if (place == 0) {
    		catchType = Bindings.createObjectBinding(() -> CatchType.EMPTY);
    	} else {
    		catchType = Bindings.createObjectBinding(() -> {
    			CatchType t;
    			if (traplineProperty.get() == null) {
    				t =  CatchType.EMPTY;
    			} else if (traplineProperty.get().getCatchTypes().size() < place) {
    	    		LOG.log(Level.WARNING, "Trapline lacks a catch type entry at place "+place+" (only "+traplineProperty.get().getCatchTypes().size()+" available)");
    				t = CatchType.EMPTY;//TODO: Currently puts another 'empty' entry down if nothing is specified. Should something else be put instead?
    			} else {
    				t = traplineProperty.get().getCatchTypes().get(place-1);
    			}    			
    			return Objects.requireNonNull(t, "Invalid catch type at "+place);//Make sure we don't have a 'null' catch type anywhere 
    		}, traplineProperty);
    	}
    	Button button = new Button();
    	button.textProperty().bind(Bindings.createStringBinding(() -> catchType.get().getName(), catchType));
    	button.setMaxSize(1000, 1000);
    	//button.getStyleClass().add("large-button");
    	GridPane.setConstraints(button, place % 2, place > 1 ? 2 : 0);
    	GridPane.setHgrow(button, Priority.ALWAYS);
    	GridPane.setVgrow(button, Priority.ALWAYS);
    	button.setOnAction(evt -> {
    		LOG.log(Level.FINE, "Selected catch: "+catchType.get());
    		catchSelectDialog.setResult(catchType.get());
    		catchSelectDialog.hide();
    	});
    	return button;
    }
    
    /**
     * Go back to the previous trap in the trapline
     */
    public void previousTrap () {
		int prevNumber = trapProperty.get().getNumber()-step;
		while (trapNumberLookup[prevNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			prevNumber -= step;
		}
		LOG.log(Level.FINE, "Requested swap to previous trap (#"+prevNumber+")");
		currentTrapIndex.set(trapNumberLookup[prevNumber]);
    }
    
    /**
     * Jump to the next trap in the trapline
     */
    public void nextTrap() {
		int nextNumber = trapProperty.get().getNumber()+step;
		while (trapNumberLookup[nextNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			nextNumber += step;
		}		
		LOG.log(Level.FINE, "Requested swap to next trap (#"+nextNumber+")");
		currentTrapIndex.set(trapNumberLookup[nextNumber]);
    }
    
    /**
     * Checks whether a trap exists prior to the selected trap
     * @return True if a previous trap exists, false if this is the first trap in the line
     */
    public boolean hasPreviousTrap () {
    	int prevNumber = trapProperty.get().getNumber()-step;
		while (canTraverseNext(step < 0, prevNumber, startTrap) && trapNumberLookup[prevNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			prevNumber -= step;
		}
    	return canTraverseNext(step < 0, prevNumber, startTrap);
    }
    
    /**
     * Checks whether another trap exists after the current one
     * @return True if a next trap exists, false if this is the last trap in the trapline
     */
    public boolean hasNextTrap () {
    	int nextNumber = trapProperty.get().getNumber()+step;
		while (canTraverseNext(step > 0, nextNumber, endTrap) && trapNumberLookup[nextNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			nextNumber += step;
		}
    	return canTraverseNext(step > 0, nextNumber, endTrap);
    }
    
    private boolean canTraverseNext (boolean reversed, int number, int limit) {
    	return reversed ? number <= limit : number >= limit;
    }
    
    /**
     * Sets the trapline for the navigation view & sets the first trap as trap #1 (or whichever is the lowest number)
     * @param trapline
     */
    public final void setTrapline (Trapline trapline) {
    	Objects.requireNonNull(trapline);
    	traplineProperty.set(trapline);
    	currentTrapIndex.set(0);
    	
    	List<Trap> trapsTmp = new ArrayList<>(trapline.getTraps());
    	
    	/*Iterator<Trap> filter = trapsTmp.iterator();    	
    	while (filter.hasNext()) {
    		//Remove inactive traps from the list
    		if (filter.next().getStatus() != TrapStatus.ACTIVE) {
    			filter.remove();
    		}
    	}*/
    	
    	Collections.sort(trapsTmp, (t1, t2) -> t1.getNumber() - t2.getNumber());
    	
    	Trap highestTrap = trapsTmp.get(trapsTmp.size()-1);
    	
    	trapNumberLookup = new int[highestTrap.getNumber()+1];
    	Arrays.fill(trapNumberLookup, -1);
    	
    	for (int i=0;i<trapsTmp.size();i++) {
    		Trap t = trapsTmp.get(i);
    		trapNumberLookup[t.getNumber()] = i;
    	}
    	startTrap = trapsTmp.get(0).getNumber();
    	endTrap = highestTrap.getNumber();
    	step = 1;
    	
    	//Sort the traps by trap number
    	orderedTraps.setAll(trapsTmp);
    }
    
    /**
     * Sets the sequence for the navigation view to use.
     * 
     * NOTE: This method must be called <b>after</b> {@link #setTrapline(Trapline)}, as setTrapline will overwrite the values set here
     * 
     * To visit all traps in sequence, startTrap should be set to 1, endTrap set to the final trap number, and step set to 1
     * To traverse the trapline backwards, startTrap should be set to the final trap number, endTrap set to 1, and step set to -1
     * To visit every even numbered trap, startTrap should be set to 2, endTrap set to the final even trap number, and step set to 2.
     * @param startTrap The first trap to visit in the trapline
     * @param endTrap The last trap to visit in the trapline
     * @param step The step to take between each trap in the trapline
     */
    public void setNavigationSequence(int startTrap, int endTrap, int step) {
    	assert step != 0 : "step is zero!";//Can't have a step of zero, else we'll never move anywhere!
    	assert Math.abs(endTrap - startTrap) >= Math.abs(step) : "step is too high! It should be no greater than the number of traps in the sequence!";
    	assert step > 0 ? startTrap <= endTrap : startTrap >= endTrap : "step runs in the wrong direction! step="+step+", end="+endTrap+", start="+startTrap;//
    	
    	this.startTrap = startTrap;
    	this.endTrap = endTrap;
    	this.step = step;
    	currentTrapIndex.set(trapNumberLookup[startTrap]);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        //appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        trapProperty.addListener((obs, oldV, newV) -> {
        	if (newV != null) {
                appBar.setTitleText("Trap "+newV.getNumber());        		
        	}
        });
        appBar.setTitleText("Trap "+trapProperty.get().getNumber());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> MobileApplication.getInstance().switchToPreviousView()));
    }
    
}
