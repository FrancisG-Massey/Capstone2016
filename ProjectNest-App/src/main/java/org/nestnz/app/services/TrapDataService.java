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
package org.nestnz.app.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.parser.ParserTrapline;
import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.converter.InputStreamInputConverter;
import com.gluonhq.connect.converter.JsonInputConverter;
import com.gluonhq.connect.converter.JsonOutputConverter;
import com.gluonhq.connect.converter.OutputStreamOutputConverter;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.FileClient;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;
import com.gluonhq.impl.connect.converter.JsonUtil;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public final class TrapDataService implements ListChangeListener<Trapline> {

    private static final Logger LOG = Logger.getLogger(TrapDataService.class.getName());
    
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList(trapline -> {
    	return new Observable[] { trapline.nameProperty(), trapline.regionProperty(), trapline.getTraps(),
    			trapline.endProperty(), trapline.startProperty() };
    });
    
    private final Map<Integer, Region> regions = new HashMap<>();
    
    private final File trapCachePath;
    
    private final LoginService loginService;
    
    private final ReadOnlyBooleanWrapper loadingProperty = new ReadOnlyBooleanWrapper(false);
    
    public TrapDataService (File trapCachePath, LoginService loginService) throws IOException {
    	Objects.requireNonNull(trapCachePath);
    	trapCachePath.mkdirs();
    	
    	this.trapCachePath = trapCachePath;
    	this.loginService = loginService;
    	loadTraplines();
    	watchForChanges();
    }
    
    public boolean isLoading () {
    	return loadingProperty.get();
    }
    
    public ReadOnlyBooleanProperty loadingProperty () {
    	return loadingProperty.getReadOnlyProperty();
    }
    
    protected void loadTraplines () {
    	int count = 0;
    	for (File traplineFile : trapCachePath.listFiles()) {
        	LOG.log(Level.INFO, String.format("File: %s", traplineFile));
        	
        	if (traplineFile.toString().endsWith(".json")) {
    			FileClient fileClient = FileClient.create(traplineFile);
    			
    			InputStreamInputConverter<ParserTrapline> converter = new JsonInputConverter<>(ParserTrapline.class);

    			GluonObservableObject<ParserTrapline> pTrapline = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
    			pTrapline.initializedProperty().addListener((obs, oldValue, newValue) -> {
    				if (newValue) {
    					try {
    						ParserTrapline pLine = pTrapline.get();
    						int rId = pLine.getRegion().getId();
    						Region r;
    						if (regions.containsKey(rId)) {
    							r = regions.get(rId);
    						} else {
    							r = new Region(rId, pLine.getRegion().getName());
    							regions.put(r.getId(), r);
    						}
    						Trapline t = new Trapline(pLine.getId(), pLine.getName(), r, pLine.getStart(), pLine.getEnd());
    						for (ParserTrap pTrap : pLine.getTraps()) {
    							LocalDateTime created = pTrap.getCreated() == null ? null : LocalDateTime.parse(pTrap.getCreated());
    							LocalDateTime reset = pTrap.getLastReset() == null ? null : LocalDateTime.parse(pTrap.getLastReset());
    							TrapStatus status = pTrap.getStatus() == null ? null : TrapStatus.valueOf(pTrap.getStatus());
    							Trap trap = new Trap(pTrap.getId(), pTrap.getNumber(), 
    									pTrap.getCoordLat(), pTrap.getCoordLong(), 
    									status, created, reset);
    							t.getTraps().add(trap);
    						}
    						addTrapline(t);
    					} catch (RuntimeException ex) {
    						LOG.log(Level.WARNING, "Failed to load data from trapline cache file "+traplineFile, ex);
    						traplineFile.delete();//This error means the cache file must be corrupted, so delete it (we can always get the data back from the server when needed)
    					}
    				}
    			});
    			count++;
    		}
    	}
    	LOG.log(Level.INFO, String.format("Found data for %d traplines in file cache", count));
    }
    
    private void addTrapline (Trapline trapline) {
    	Iterator<Trapline> it = traplines.iterator();
    	while (it.hasNext()) {
    		if (it.next().getId() == trapline.getId()) {
    			it.remove();
    		}
    	}
    	traplines.add(trapline);
    }
    
    /**
     * Fetches a trapline based on its ID. 
     * This method only checks the memory cache for traplines - it doesn't check whether a trapline has been cached on the disk or exists on the server.
     * @param id The ID of the trapline to lookup
     * @return The trapline with the matching ID, or null if no trapline could be found
     */
    public Trapline getTrapline (int id) {
    	for (Trapline t : traplines) {
    		if (t.getId() == id) {
    			return t;
    		}
    	}
    	return null;
    }
    
    /**
     * Requests an update for trapline metadata for all traplines this user can access.
     * Note: This only updates the trapline metadata (name, ID, region, etc) - NOT the catch data or the traps themselves.
     * This method is generally used to add traplines the user can now access, or remove those they can no longer access
     */
    public void refreshTraplines () {
    	if (loadingProperty.get()) {
    		return;
    	}
    	loadingProperty.set(true);
    	
    	refreshRegions();//Reload the regions first
    	
    	RestClient traplineClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/trapline").header("Session-Token", loginService.getSessionToken());
    	
    	RestDataSource dataSource = traplineClient.createRestDataSource();
    	
    	BackgroundTasks.runInBackground(() -> {
    		try (JsonReader reader = JsonUtil.createJsonReader(dataSource.getInputStream())) {
    			JsonArray array = reader.readArray();
    			LOG.log(Level.INFO, "Response: "+array.toString());
    			Platform.runLater(() -> {
    				for (JsonValue value : array) {
        				JsonObject traplineJson = (JsonObject) value;
        				int id = traplineJson.getInt("id");
        				Trapline trapline = getTrapline(id);
        				if (trapline == null) {
        					trapline = new Trapline(id);
        					addTrapline(trapline);
        				}
        				trapline.setName(traplineJson.getString("name"));
        				trapline.setStart(traplineJson.getString("start_tag"));
        				trapline.setEnd(traplineJson.getString("end_tag"));
        				int regionId = traplineJson.getInt("region_id");
        				Region region = regions.get(regionId);
        				if (region == null) {
        					region = new Region(regionId);
        					regions.put(regionId, region);
        				}
        				trapline.setRegion(region);
        			}
    				loadingProperty.set(false);
    			});
    		} catch (IOException | RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Problem requesting traplines. Response: "+dataSource.getResponseMessage(), ex);
				loadingProperty.set(false);
			}
    	});
    }
    
    public void loadTrapline (Trapline trapline) {
    	if (loadingProperty.get()) {
    		return;
    	}
    	loadingProperty.set(true);
    	
    	RestClient trapsClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/trap").header("Session-Token", loginService.getSessionToken());
    	
    	RestDataSource dataSource = trapsClient.createRestDataSource();
    	BackgroundTasks.runInBackground(() -> {
    		try (JsonReader reader = JsonUtil.createJsonReader(dataSource.getInputStream())) {
    			JsonArray array = reader.readArray();
    			LOG.log(Level.INFO, "Response: "+array.toString());
    			Platform.runLater(() -> {
    				for (JsonValue value : array) {
        				JsonObject trapJson = (JsonObject) value;
        				if (trapJson.getInt("trapline_id") != trapline.getId()) {
        					continue;//If the trap doesn't belong to the specified trapline, ignore it
        				}
        				int id = trapJson.getInt("id");
        				int number = trapJson.getInt("number");
        				double latitude = Double.parseDouble(trapJson.getString("coord_lat"));
        				double longitude = Double.parseDouble(trapJson.getString("coord_long"));
        				LocalDateTime created = LocalDateTime.parse(trapJson.getString("created").replace(' ', 'T'));
        				LocalDateTime lastReset = LocalDateTime.parse(trapJson.getString("last_reset").replace(' ', 'T'));
        				Trap trap = trapline.getTrap(id);
        				if (trap == null) {
        					trap = new Trap(id, number, latitude, longitude, TrapStatus.ACTIVE, created, lastReset);
        					trapline.getTraps().add(trap);
        				} else {
        					trap.setNumber(number);
        					trap.setLatitude(latitude);
        					trap.setLongitude(longitude);
        					trap.setLastReset(lastReset);
        				}
        			}
    				loadingProperty.set(false);
    			});
    		} catch (IOException | RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Problem requesting traps for trapline "+trapline+". Response: "+dataSource.getResponseMessage(), ex);
				loadingProperty.set(false);
			}
    	});
    }
    
    protected void refreshRegions () {
    	RestClient regionClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/region").header("Session-Token", loginService.getSessionToken());
    	
    	RestDataSource dataSource = regionClient.createRestDataSource();
    	
    	BackgroundTasks.runInBackground(() -> {
    		try (JsonReader reader = JsonUtil.createJsonReader(dataSource.getInputStream())) {
    			JsonArray array = reader.readArray();
    			LOG.log(Level.INFO, "Response: "+array.toString());
    			Platform.runLater(() -> {
    				for (JsonValue value : array) {
        				JsonObject regionJson = (JsonObject) value;
        				int id = regionJson.getInt("id");
        				Region region = regions.get(id);
        				if (region == null) {
        					region = new Region(id);
        					regions.put(id, region);
        				}
        				region.setName(regionJson.getString("name"));
        			}
    				for (Trapline t : traplines) {
    					Region r = t.getRegion();
    					t.setRegion(null);
    					t.setRegion(r);//Forcefully update all the regions
    				}
    			});
    		} catch (IOException | RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Problem requesting regions. Response: "+dataSource.getResponseMessage(), ex);
			}
    	});
    }

	public final ObservableList<Trapline> getTraplines() {
		return traplines;
	}
	
	
	private final Set<Trapline> updatedTraplines = new HashSet<>();
    
    private void watchForChanges () {
    	traplines.addListener(this);//Listen for changes to the traplines
    	
    	//Every 5 seconds, check if there are any traplines awaiting update. If there are, save them to the cache 
    	BackgroundTasks.scheduleRepeating(() -> {
    		if (!updatedTraplines.isEmpty()) {
	    		Set<Trapline> updatesCopy = new HashSet<>(updatedTraplines);
	    		updatedTraplines.clear();
	    		
	    		for (Trapline t : updatesCopy) {
	    			updateTrapline(t);
	    		}
	    		LOG.log(Level.INFO, "Saved "+updatesCopy.size()+" traplines to file cache.");
    		}
    	}, 5, TimeUnit.SECONDS);
    }

	/* (non-Javadoc)
	 * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
	 */
	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Trapline> c) {
		while (c.next()) {
			if (c.wasAdded()) {
				for (Trapline t : c.getAddedSubList()) {
					updatedTraplines.add(t);
				}
			} else if (c.wasUpdated()) {
				for (Trapline t : c.getAddedSubList()) {
					updatedTraplines.add(t);
				}
			}
		}
	}
	
	/**
	 * Updates the cached version of the trapline and flags the trapline as dirty
	 * @param trapline The trapline to update
	 * @return a {@link GluonObservableObject} which is set when the object is fully written
	 */
	public GluonObservableObject<ParserTrapline> updateTrapline (Trapline trapline) {
		File savedFile = new File(trapCachePath, Integer.toString(trapline.getId())+".json");
		
		
		FileClient fileClient = FileClient.create(savedFile);
		
		OutputStreamOutputConverter<ParserTrapline> outputConverter = new JsonOutputConverter<>(ParserTrapline.class);

		GluonObservableObject<ParserTrapline> result = DataProvider.storeObject(new ParserTrapline(trapline), fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved trapline data for %s to %s", trapline.getName(), savedFile));		
    	return result;
	}
}
