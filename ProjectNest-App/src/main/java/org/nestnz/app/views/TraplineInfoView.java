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

import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.beans.binding.Bindings.format;
import static javafx.beans.binding.Bindings.isNotEmpty;
import static javafx.beans.binding.Bindings.size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.services.TrapDataService;
import org.nestnz.app.services.TraplineMonitorService;
import org.nestnz.app.util.Sequence;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class TraplineInfoView extends View {

    private static final Logger LOG = Logger.getLogger(TraplineInfoView.class.getName());
	
	/**
	 * Represents the number of hours between automatically fetching the trapline data from the server.
	 * This only occurs if the view is closed & re-opened after at least the frequency has passed since the last refresh.
	 */
	public static final int REFRESH_FREQUENCY = 24;
	
	public static final String NAME = "trapline_info";
	
	private Trapline trapline;
	
	private Button start = new Button("Start");
	
    private Label traplineSize = new Label();
	
    private Label lastUpdated = new Label();
    
    private Button preloadMap = new Button("Map Loaded");
    
    private Button sendCatchLogs = new Button("Send Catch Logs");
    
    private Button sendTraps = new Button("Send Created Traps");
	
	
	private final TrapDataService dataService;
	
	private final MapLoadingService mapService;
	
	private double minLat = Double.MAX_VALUE;
    private double maxLat = Double.MIN_VALUE;
    private double minLong = Double.MAX_VALUE;
    private double maxLong = Double.MIN_VALUE;

    private IntegerProperty mapTileTotalCount = new SimpleIntegerProperty();
    private IntegerProperty mapTileLoadedCount = new SimpleIntegerProperty();
    
	public TraplineInfoView(TrapDataService dataService, MapLoadingService mapService) {
		super(NAME);
		this.dataService = Objects.requireNonNull(dataService);
		this.mapService = Objects.requireNonNull(mapService);
		
        this.setOnShown(evt -> {
    		if (trapline != null) {
    			Optional<LocalDateTime> lastUpdated = trapline.getLastUpdated();
    			if (dataService.isNetworkAvailable() &&
    					(!lastUpdated.isPresent() || lastUpdated.get().plusHours(REFRESH_FREQUENCY).isBefore(LocalDateTime.now()))) {
    				refreshTrapline();
    			}
    		}
        });
        
        initControls();
	}
    
    private void initControls () {
    	VBox controls = new VBox();
    	
        preloadMap.setOnAction(evt -> {
        	ReadOnlyIntegerProperty remaining = mapService.preloadMapTiles(minLat, maxLat, minLong, maxLong);
        	getApplication().showLayer("loading");
        	remaining.addListener((obs, oldVal, newVal) -> {
        		mapTileLoadedCount.set(mapTileTotalCount.get()-newVal.intValue());
        		if (newVal.intValue() <= 0) {
        			getApplication().hideLayer("loading");
        		}
        	});
        	if (remaining.get() <= 0) {
        		getApplication().hideLayer("loading");
        	}
        });
        sendCatchLogs.setOnAction(evt -> 
	    	//Send any unsent catch logs to the server
	    	dataService.getTraplineUpdateService(trapline).sendCatchesToServer().addListener((obs, oldStatus, newStatus) -> {
	    		NestApplication app = (NestApplication) this.getApplication();
	        	switch (newStatus) {
				case PENDING:
					app.showLayer("loading");
					break;
				case SUCCESS:
					app.hideLayer("loading");
					break;
				case FAILED_NETWORK:
					app.hideLayer("loading");
					app.showNotification("Unable to reach the NestNZ server. Please make sure you have internet access before trying again.");
					break;
				case FAILED_OTHER:
				case FAILED_UNAUTHORISED:
					app.hideLayer("loading");
					app.showNotification("There was a problem sending catch logs for this trapline. Please try again later.");
					break;
	    		}
	    	})
	    );
	    sendTraps.setOnAction(evt -> 
	    	//Send any unsent created traps to the server
	    	dataService.getTraplineUpdateService(trapline).sendTrapsToServer().addListener((obs, oldStatus, newStatus) -> {
	    		NestApplication app = (NestApplication) this.getApplication();
	        	switch (newStatus) {
				case PENDING:
					app.showLayer("loading");
					break;
				case SUCCESS:
					app.hideLayer("loading");
					break;
				case FAILED_NETWORK:
					app.hideLayer("loading");
					app.showNotification("Unable to reach the NestNZ server. Please make sure you have internet access before trying again.");
					break;
				case FAILED_OTHER:
				case FAILED_UNAUTHORISED:
					app.hideLayer("loading");
					app.showNotification("There was a problem sending newly created traps on this trapline. Please try again later.");
					break;
	    		}
	    	})
	    );
        controls.getStyleClass().add("trapline-info-vbox");
        
        
        Label trapSizeHeading = new Label("Traps");
        trapSizeHeading.getStyleClass().add("heading");
        Label lastUpdatedHeading = new Label("Last Updated");
        lastUpdatedHeading.getStyleClass().add("heading");
        
        Label actionsHeading = new Label("Actions");
        actionsHeading.getStyleClass().add("heading");
        
        controls.getChildren().addAll(trapSizeHeading, traplineSize, lastUpdatedHeading, 
        		lastUpdated, actionsHeading, preloadMap, sendCatchLogs, sendTraps);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setContent(controls);
        setCenter(scrollPane);
        
        preloadMap.textProperty().bind(createStringBinding(() -> {
        	int loaded = mapTileLoadedCount.get();
        	int total = mapTileTotalCount.get();
        	double percent = (loaded+0.0)/total*100;
        	if (total == Integer.MAX_VALUE) {
        		preloadMap.setDisable(true);
        		return "Can't Preload Map";
        	} else if (total == 0) {
        		preloadMap.setDisable(true);
        		return "No traps exist!";
        	} else if (percent >= 100) {
        		preloadMap.setDisable(true);
        		return "Map Loaded";
        	} else {
        		preloadMap.setDisable(false);
        		return String.format("Preload Map (%.0f%%)", percent);
        	}
        }, mapTileLoadedCount, mapTileTotalCount));
        
        start.getStyleClass().add("large-button");
		start.setOnAction(evt -> {
			//Find the highest & lowest trap numbers in the trapline
			int minTrap = Integer.MAX_VALUE;
			int maxTrap = 0;
			for (Trap t : trapline.getTraps()) {
				if (t.getNumber() > maxTrap) {
					maxTrap = t.getNumber();
				}
				if (t.getNumber() < minTrap) {
					minTrap = t.getNumber();
				}
			}
			//Create the dialog for selecting the start & end points of the navigation sequence
			Dialog<Sequence> dialog = buildSequenceDialog(minTrap, maxTrap);
			
			//If the "Go" button was pressed, swap to the navigation view using the supplied start & end trap numbers
			dialog.showAndWait().ifPresent(seq -> {
				NavigationView navView = ((NestApplication) getApplication()).lookupView(NavigationView.NAME);
				navView.setTrapline(trapline);
				navView.setNavigationSequence(seq.getStart(), seq.getEnd(), seq.getStep());
				getApplication().switchView(NavigationView.NAME);
			});			
		});
		this.setBottom(start);
    }
	
	public void setTrapline (Trapline trapline) {
		this.trapline = trapline;
		start.visibleProperty().bind(isNotEmpty(trapline.getTraps()));
        traplineSize.textProperty().bind(format("%d", size(trapline.getTraps())));
        
        lastUpdated.textProperty().bind(createStringBinding(() -> {
        	String time;
        	if (trapline.getLastUpdated().isPresent()) {
        		LocalDateTime lastUpdated = trapline.getLastUpdated().get();
            	time = lastUpdated.format(DateTimeFormatter.ofPattern("EEEE, d MMMM h:mm a"));
        	} else {
        		time = "Never";
        	}
        	return String.format("%s", time);
        }, trapline.lastUpdatedProperty()));
        
        TraplineMonitorService monitorService = dataService.getTraplineUpdateService(trapline);
        sendCatchLogs.visibleProperty().bind(Bindings.isNotEmpty(monitorService.getUnsentCatchLogs()));
        sendCatchLogs.textProperty().bind(Bindings.format("Send %d Catch Logs", Bindings.size(monitorService.getUnsentCatchLogs())));
        
        sendTraps.visibleProperty().bind(Bindings.isNotEmpty(monitorService.getUnsentTraps()));
        sendTraps.textProperty().bind(Bindings.format("Send %d Created Traps", Bindings.size(monitorService.getUnsentTraps())));
        
        updateMapLoadProgress();
	}
	
	private void updateMapLoadProgress () {
        if (trapline.getTraps().isEmpty()) {
        	mapTileTotalCount.set(0);
        	mapTileLoadedCount.set(0);
        } else {
	        minLat = Double.MAX_VALUE;
	        maxLat = Double.MIN_VALUE;
	        minLong = Double.MAX_VALUE;
	        maxLong = Double.MIN_VALUE;
	        
	        for (Trap t : trapline.getTraps()) {
	        	minLat = absMin(minLat, t.getLatitude());
	        	maxLat = absMax(maxLat, t.getLatitude());
	        	minLong = absMin(minLong, t.getLongitude());
	        	maxLong = absMax(maxLong, t.getLongitude());
	        }
	        LOG.log(Level.INFO, "Map Range: "+minLat+","+minLong+" to "+minLat+","+minLong);
	        int totalTiles = mapService.getTotalTileCount(minLat, maxLat, minLong, maxLong);
	        if (totalTiles < MapLoadingService.MAX_TILES) {
	        	int cachedTiles = mapService.getCachedTileCount(minLat, maxLat, minLong, maxLong);
	        	mapTileTotalCount.set(totalTiles);
	        	mapTileLoadedCount.set(cachedTiles);
	        } else {
	        	LOG.log(Level.WARNING, "Trapline "+trapline.getId()+" covers "+totalTiles+" map tiles!");
	        	mapTileTotalCount.set(Integer.MAX_VALUE);
	        	mapTileLoadedCount.set(Integer.MAX_VALUE);
	        }	        
        }
	}
	
	
	private Dialog<Sequence> buildSequenceDialog (int minTrap, int maxTrap) {
		Dialog<Sequence> dialog = new Dialog<>();
		
    	GridPane controls = new GridPane();
    	dialog.setContent(controls);
    	dialog.setTitleText("Select Navigation Sequence");
    	
    	ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        controls.getColumnConstraints().addAll(column1, column2);
        
        Label startTrapLabel = new Label("Start Trap:");
        Spinner<Integer> startTrapSelector = new Spinner<>(minTrap, maxTrap, minTrap);
        startTrapSelector.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        
        Label endTrapLabel = new Label("End Trap:");
        Spinner<Integer> endTrapSelector = new Spinner<>(minTrap, maxTrap, maxTrap);
        endTrapSelector.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        
        GridPane.setConstraints(startTrapLabel, 0, 0);
        GridPane.setConstraints(startTrapSelector, 1, 0);
        
        GridPane.setConstraints(endTrapLabel, 0, 1);
        GridPane.setConstraints(endTrapSelector, 1, 1);
        
		controls.getChildren().addAll(startTrapLabel, startTrapSelector, endTrapLabel, endTrapSelector);
		
		Button go = new Button("Go");
		Button cancel = new Button("Cancel");
		
		go.setOnAction(evt -> {
			int startNum = startTrapSelector.getValue();
			int endNum = endTrapSelector.getValue();
			int step = 1;
			
			if (startNum > endNum) {
				//If a higher start point has been selected (compared to the end point), run the trapline sequence in reverse
				step = -1;
			}
			dialog.setResult(new Sequence(startNum, endNum, step));
			dialog.hide();
		});
		
		cancel.setOnAction(evt -> dialog.hide());
		
		dialog.getButtons().addAll(go, cancel);
		return dialog;
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
		appBar.setTitleText(trapline.getName());
		if (trapline.canEdit()) {
			appBar.getActionItems().add(MaterialDesignIcon.ADD.button(evt -> {
				AddTrapView addTrapView = ((NestApplication) getApplication()).lookupView(AddTrapView.NAME);
				addTrapView.setTrapline(trapline);
				getApplication().switchView(AddTrapView.NAME);
			}));
		}
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> refreshTrapline()));

    }
    
    private void refreshTrapline () {
    	NestApplication app = (NestApplication) this.getApplication();
    	dataService.refreshTrapline(trapline).addListener((obs, oldStatus, newStatus) -> {
    		String message = null;
    		switch (newStatus) {
			case PENDING:
				return;
			case FAILED_NETWORK:
				message = "Unable to reach the NestNZ server. Please make sure you have internet access before trying again.";
				break;
			case FAILED_OTHER:
				message = "There was a problem loading traps for the "+trapline.getName()+" trapline. Please try again later.";
				break;
			case FAILED_UNAUTHORISED:
				message = "You cannot view this trapline.";
				break;
			case SUCCESS:
				break;    		
    		}
			updateMapLoadProgress();
    		app.hideLayer("loading");
    		if (message != null) {
    			app.showNotification(message);
    		}
    	});
    	app.showLayer("loading");
    }
	
	/**
	 * Compares the provided values and returns the one furtherest from zero
	 * @param val1 The first value
	 * @param val2 The second value
	 * @return Whichever of val1 and val2 is furtherest from zero
	 */
	private static final double absMax (double val1, double val2) {
		return Math.abs(val1) < Math.abs(val2) ? val2 : val1;
	}
	
	/**
	 * Compares the provided values and returns the one closest to zero
	 * @param val1 The first value
	 * @param val2 The second value
	 * @return Whichever of val1 and val2 is closest to zero
	 */
	private static final double absMin (double val1, double val2) {
		return Math.abs(val1) > Math.abs(val2) ? val2 : val1;
	}

}
