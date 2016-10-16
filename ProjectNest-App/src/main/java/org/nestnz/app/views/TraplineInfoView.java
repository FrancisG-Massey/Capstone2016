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
import org.nestnz.app.util.Sequence;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.layout.layer.MenuSidePopupView;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TraplineInfoView extends View implements ChangeListener<Boolean> {

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
    
    private Label mapLoaded = new Label();
    
    private Button preloadMap = new Button("Preload");
	
	private final SidePopupView menu;
	
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
    		dataService.loadingProperty().addListener(this);
    		if (trapline != null) {
    			Optional<LocalDateTime> lastUpdated = trapline.getLastUpdated();
    			if (!lastUpdated.isPresent() || lastUpdated.get().plusHours(REFRESH_FREQUENCY).isBefore(LocalDateTime.now())) {
    				dataService.loadTrapline(trapline);
    			}
    		}
        });
        
        this.setOnHidden(evt -> {
        	dataService.loadingProperty().removeListener(this);
        });
        
        initControls();
        
		menu = buildMenu();
		getApplication().addLayerFactory("trapline-info-menu", () -> menu);
        getStylesheets().add(TraplineListView.class.getResource("styles.css").toExternalForm());
	}
    
    private void initControls () {
    	VBox controls = new VBox();
        HBox mapLoadControls = new HBox();
        preloadMap.setVisible(false);
        preloadMap.setOnAction(evt -> {
        	ReadOnlyIntegerProperty remaining = mapService.preloadMapTiles(minLat, maxLat, minLong, maxLong);
        	getApplication().showLayer("loading");
        	remaining.addListener((obs, oldVal, newVal) -> {
        		mapTileLoadedCount.set(mapTileTotalCount.get()-newVal.intValue());
        		if (newVal.intValue() <= 0) {
        			getApplication().hideLayer("loading");
        			preloadMap.setVisible(false);
        		}
        	});
        	if (remaining.get() <= 0) {
        		getApplication().hideLayer("loading");
        	}
        });
        mapLoadControls.getChildren().addAll(mapLoaded, preloadMap);
        controls.getChildren().addAll(traplineSize, lastUpdated, mapLoadControls);
        setCenter(controls);
        
        mapLoaded.textProperty().bind(createStringBinding(() -> {
        	int loaded = mapTileLoadedCount.get();
        	int total = mapTileTotalCount.get(); 
        	if (total == Integer.MAX_VALUE) {
        		return "Map Loaded: Unknown";
        	} else if (total == 0) {
        		return "Map Loaded: 0% (0/0)";
        	} else {
        		return String.format("Map Loaded: %.0f%% (%d/%d)", (loaded+0.0)/total*100, loaded, total);
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
        traplineSize.textProperty().bind(format("Traps: %d", size(trapline.getTraps())));
        
        lastUpdated.textProperty().bind(createStringBinding(() -> {
        	String time;
        	if (trapline.getLastUpdated().isPresent()) {
        		LocalDateTime lastUpdated = trapline.getLastUpdated().get();
            	time = lastUpdated.format(DateTimeFormatter.ofPattern("EEEE, d MMMM h:mm a"));
        	} else {
        		time = "Never";
        	}
        	return String.format("Last fetched: %s", time);
        }, trapline.lastUpdatedProperty()));
        
        updateMapLoadProgress();
	}
	
	private void updateMapLoadProgress () {
		preloadMap.setVisible(false);
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
		        //int mapLoadedPercent = (int) ((cachedTiles+0.0) / totalTiles)*100;
	        	mapTileTotalCount.set(totalTiles);
	        	mapTileLoadedCount.set(cachedTiles);
		        
		        //mapLoaded.setText("Map Loaded: "+mapLoadedPercent+"% ("+cachedTiles+"/"+totalTiles+")");
	        	preloadMap.setVisible(cachedTiles < totalTiles);
	        } else {
	        	LOG.log(Level.WARNING, "Trapline "+trapline.getId()+" covers "+totalTiles+" map tiles!");
	        	mapTileTotalCount.set(Integer.MAX_VALUE);
	        	mapTileLoadedCount.set(Integer.MAX_VALUE);
	        }	        
        }
	}
	
	private SidePopupView buildMenu () {
		Menu menu = new Menu();
		final MenuItem addTraps = new MenuItem("Add Traps", MaterialDesignIcon.ADD.graphic());
		
		addTraps.setOnAction(evt -> {
			this.getApplication().hideLayer("trapline-info-menu");
			AddTrapView addTrapView = ((NestApplication) getApplication()).lookupView(AddTrapView.NAME);
			addTrapView.setTrapline(trapline);
			getApplication().switchView(AddTrapView.NAME);
		});
		
		menu.getItems().add(addTraps);
		return new MenuSidePopupView(menu, Side.LEFT);
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
        
        Label endTrapLabel = new Label("End Trap:");
        Spinner<Integer> endTrapSelector = new Spinner<>(minTrap, maxTrap, maxTrap);
        
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
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> this.getApplication().showLayer("trapline-info-menu")));
		appBar.setTitleText(trapline.getName());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> dataService.loadTrapline(trapline)));
    }

	/* (non-Javadoc)
	 * @see javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		if (newValue) {
			this.getApplication().showLayer("loading");
		} else {
			this.getApplication().hideLayer("loading");
			updateMapLoadProgress();
		}
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
