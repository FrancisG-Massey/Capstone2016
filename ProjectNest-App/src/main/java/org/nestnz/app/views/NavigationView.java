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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.services.TrapDataService;
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

import javafx.beans.binding.Bindings;
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
        
    /**
     * The button used to access the previous trap in the trapline
     */
    final Button prev = MaterialDesignIcon.ARROW_BACK.button(evt -> previousTrap());;
    
    /**
     * The button used to access the next trap in the trapline
     */
    final Button next = MaterialDesignIcon.ARROW_FORWARD.button(evt -> nextTrap());
    
    /**
     * Used to quickly look up an index in {@link #orderedTraps} based on a trap number.
     * Should always be initialised to an array the size of the maximum trap number
     */
    int[] trapNumberLookup;
    
    /**
     * The number of the first active trap in the trapline
     */
    int firstTrap;
    
    /**
     * The number of the last active trap in the trapline
     */
    int lastTrap;
    
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
    
    /**
     * The trapline currently selected for navigation
     */
    final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
    
    /**
     * Contains all traps in the trapline ordered by number
     */
    final ObservableList<Trap> orderedTraps;
    
    /**
     * The index of {@link #orderedTraps} used to find the currently selected trap
     */
    final IntegerProperty currentTrapIndex = new SimpleIntegerProperty(0);
    
    /**
     * The currently selected trap in the trapline
     */
    final ObjectProperty<Trap> trapProperty = new SimpleObjectProperty<>();
    
    /**
     * The coordinates of the selected trap
     */
    final ObjectProperty<Position> targetCoordsProperty = new SimpleObjectProperty<>();
    
    /**
     * The distance, in meters, to the selected trap defined in {@link #trapProperty}
     */
    final DoubleProperty distanceToTrap = new SimpleDoubleProperty();
    
    final Dialog<CatchType> catchSelectDialog;
	
    /**
     * The map used to show the user's current position relative to nearby traps
     */
	final MapView map = new MapView();
	
	/**
	 * The layer used for laying out the user's current position & the position of nearby traps
	 */
	final TrapPositionLayer trapPositionLayer = new TrapPositionLayer();
	
	/**
	 * The provider of all trapline data. Used in NavigationView specifically for displaying the list of catch types
	 */
	final TrapDataService trapDataService; 

    public NavigationView(TrapDataService dataService) {
        this(dataService, false);
    }
    
    protected NavigationView(TrapDataService dataService, boolean test) {
    	super(NAME);
    	
    	this.trapDataService = Objects.requireNonNull(dataService);
    	
    	orderedTraps = trapPositionLayer.getTraps();
        //setShowTransitionFactory(BounceInRightTransition::new);
        
        //getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
        //    e -> System.out.println("Info")));
        
        initControls();
        if (!test) {
        	initMonitors();
        	CatchSelectionDialog catchSelectDialog = new CatchSelectionDialog();
        	
            traplineProperty.addListener((obs, oldLine, newLine) -> {
            	//Watch for trapline changes. 
            	//When found, change the order of catch types so the most common ones for the trapline appear at the top
            	if (newLine != null) {
            		catchSelectDialog.getCatchTypes().clear();
	            	Set<CatchType> catchTypes = new HashSet<>(dataService.getCatchTypes().values());
	            	for (CatchType ct : newLine.getCatchTypes()) {
	            		catchTypes.remove(ct);
	            		catchSelectDialog.getCatchTypes().add(ct);
	            	}
	            	catchSelectDialog.getCatchTypes().addAll(catchTypes);
            	}
            });
            this.catchSelectDialog = catchSelectDialog;
        } else {
        	catchSelectDialog = null;
        }
    }
    
    /**
     * Constructs & adds the controls used by the view
     */
    private void initControls () {
    	HBox topBox = new HBox();
    	topBox.setId("top-box");
    	
    	topBox.setAlignment(Pos.CENTER);
    	
    	Label distanceLabel = new Label("0.0");
        distanceLabel.setId("distance-label");
        distanceLabel.visibleProperty().bind(Bindings.isNotNull(trapPositionLayer.currentPositionProperty()));
        
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
    	
    	
    	trapProperty.addListener((obs, oldV, newV) -> {
    		LOG.log(Level.INFO, "Selected trap: "+newV);
    		if (newV == null) {
    			targetCoordsProperty.set(null);
    		} else {
    			targetCoordsProperty.set(new Position(newV.getLatitude(), newV.getLongitude()));
    			prev.setVisible(hasPreviousTrap(false));
    			next.setVisible(hasNextTrap(false));
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
    
    /**
     * Start listening for new GPS positions
     */
    private void initMonitors () {
    	Optional<PositionService> gpsService = Services.get(PositionService.class);
    	if (!gpsService.isPresent()) {
    		Label label = new Label("GPS is not supported on this device!");
    		label.getStyleClass().add("gps-notice");
    		setCenter(label);
    	}
    	gpsService.ifPresent(service -> {
    		trapPositionLayer.currentPositionProperty().bind(service.positionProperty());
    		Label label = new Label("Waiting for GPS coordinates...\nMake sure you have location services turned on");
    		label.getStyleClass().add("gps-notice");
			setCenter(service.getPosition() == null ? label : map);
        	
    		Bindings.isNotNull(service.positionProperty()).addListener((obs, wasPresent, isPresent) -> {
    			if (isPresent) {
    				setCenter(map);
    			} else {    				
    				setCenter(label);
    			}
    		});
    		
        	distanceToTrap.bind(Bindings.createDoubleBinding(() -> service.getPosition() == null || targetCoordsProperty.get() == null ? Double.POSITIVE_INFINITY :
        				getDistance(service.getPosition(), targetCoordsProperty.get()), 
        					service.positionProperty(), targetCoordsProperty));
    	});
    }
    
    /**
     * Displays a dialog prompting the user to select the catch type for the current trap
     */
    private void showLogCatchDialog () {
		Trap forTrap = trapProperty.get();
    	catchSelectDialog.setTitleText(String.format("Log catch #%d", forTrap.getNumber()));
    	catchSelectDialog.showAndWait().ifPresent(catchType -> {
        	if (catchType != CatchType.EMPTY) {
        		Catch loggedCatch = new Catch(catchType);
        		forTrap.getCatches().add(loggedCatch);
        	}
        	if (hasNextTrap()) {
        		nextTrap();
        	}
        	getApplication().showMessage(String.format("Logged %s in trap #%d", 
        			catchType.getName(), forTrap.getNumber())/*, "Change", evt -> {
        		modifyCatch(loggedCatch);
        	}*/);
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
     * Go back to the previous trap in the trapline
     * WARNING: This method assumes a "previous trap" exists, so {@link #hasPreviousTrap()} should always be called before calling this method
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
     * Jump to the next trap in the trapline.
     * WARNING: This method assumes a "next trap" exists, so {@link #hasNextTrap()} should always be called before calling this method
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
     * Uses the sequence set by {@link #setNavigationSequence(int, int, int)} to determine the first trap to use
     * @return True if a previous trap exists, false if this is the first trap available
     */
    public boolean hasPreviousTrap () {
    	return hasPreviousTrap(true);
    }
    
    /**
     * Checks whether a trap exists before to the selected trap
     * If useSequence is true, uses the start trap set by {@link #setNavigationSequence(int, int, int)} to determine the first trap
     * If useSequence is false, uses the first trap in the trapline as the first trap
     * @param useSequence Whether to use the sequence set by {{@link #setNavigationSequence(int, int, int)}
     * @return True if a previous trap exists, false if this is the first trap available
     */
    public boolean hasPreviousTrap (boolean useSequence) {
    	int limit = useSequence ? startTrap : firstTrap;
    	int prevNumber = trapProperty.get().getNumber()-step;
		while (canReach(step > 0, prevNumber, limit) && trapNumberLookup[prevNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			prevNumber -= step;
		}
    	return canReach(step > 0, prevNumber, limit);
    }
    
    /**
     * Checks whether another trap exists after the selected trap.
     * Uses the sequence set by {@link #setNavigationSequence(int, int, int)} to determine the last trap to use
     * @return True if a next trap exists, false if this is the last trap available
     */
    public boolean hasNextTrap () {
    	return hasNextTrap(true);
    }
    
    /**
     * Checks whether another trap exists after the current one.
     * If useSequence is true, uses the end trap set by {@link #setNavigationSequence(int, int, int)} to determine the last trap
     * If useSequence is false, uses the last trap in the trapline as the last trap
     * @param useSequence Whether to use the sequence set by {{@link #setNavigationSequence(int, int, int)}
     * @return True if a next trap exists, false if this is the last trap available
     */
    public boolean hasNextTrap (boolean useSequence) {
    	int limit = useSequence ? endTrap : lastTrap;
    	int nextNumber = trapProperty.get().getNumber()+step;
		while (canReach(step < 0, nextNumber, limit) && trapNumberLookup[nextNumber] == -1) {
			//Skip traps which don't exist (or are currently inactive)
			nextNumber += step;
		}
    	return canReach(step < 0, nextNumber, limit);
    }
    
    /**
     * Checks whether the specified trap number can be reached, given the specified limit.
     * Helper method for {@link #hasNextTrap(boolean)} and {@link #hasPreviousTrap(boolean)}
     * 
     * @param reversed True if the check should be run backwards (i.e. make sure limit is less than the desired number)
     * @param number The number of the desired trap
     * @param limit The maximum number available in the sequence
     * @return True if {@code number} can be reached, false otherwise
     */
    private boolean canReach (boolean reversed, int number, int limit) {
    	return reversed ? number >= limit : number <= limit ;
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
    	firstTrap = startTrap = trapsTmp.get(0).getNumber();
    	lastTrap = endTrap = highestTrap.getNumber();
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
    	
    	if (step < 0 != this.step < 0) {
    		//If the navigation is to run in reverse and wasn't previously (or vice versa), swap the first & last trap numbers around.
    		int tmp = this.firstTrap;
    		this.firstTrap = this.lastTrap;
    		this.lastTrap = tmp;
    	}
    	
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
