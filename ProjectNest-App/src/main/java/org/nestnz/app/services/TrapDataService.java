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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.Cacheable;
import org.nestnz.app.parser.ParserCatch;
import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.parser.ParserTrapline;
import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.connect.GluonObservableObject;
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
    			trapline.endProperty(), trapline.startProperty(), trapline.getCatchTypes() };
    });
    
    private final Map<Integer, Region> regions = new HashMap<>();
    
    private final TraplineUpdateService apiUpdateMonitor = new TraplineUpdateService();
    
    private final LoginService loginService;
    
    private final CachingService cachingService;
    
    private final ReadOnlyBooleanWrapper loadingProperty = new ReadOnlyBooleanWrapper(false);
    
    private final Cacheable<Map<Integer, CatchType>> catchTypes = new Cacheable<>();
    
    /**
     * A semaphore used to ensure regions & catch types have been loaded from the server before setting up traplines
     */
    private final Semaphore appDataLoading = new Semaphore(0);
    
    public TrapDataService (CachingService cachingService, LoginService loginService) throws IOException {
    	this.cachingService = Objects.requireNonNull(cachingService);
    	this.loginService = Objects.requireNonNull(loginService);
    }
    
    public void initialise () {
    	GluonObservableObject<Cacheable<Map<Integer, CatchType>>> results = cachingService.fetchCatchTypes();
    	
    	if (results.isInitialized()) {//This means no cached file was found
    		this.catchTypes.setData(new HashMap<>());
    	}
    	
    	results.initializedProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue) {
    			this.catchTypes.setData(results.get().getData());
    			this.catchTypes.setLastServerFetch(results.get().getLastServerFetch());
    		}
    	});
    	cachingService.fetchTraplines((pTrapline) -> {
    		addTrapline(pTrapline);
    	});
    	watchForChanges();
    }
    
    private void addTrapline (ParserTrapline pLine) {
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
			for (ParserCatch pCatch : pTrap.getCatches()) {
				CatchType cType = catchTypes.getData().get(pCatch.getTypeId());			
				LocalDateTime timestamp = LocalDateTime.parse(pCatch.getTimestamp());
				trap.getCatches().add(new Catch(cType, timestamp));
			}
			t.getTraps().add(trap);
		}
		for (long catchTypeId : pLine.getCatchTypes()) {
			CatchType cType = catchTypes.getData().get(Integer.valueOf((int) catchTypeId));			
			t.getCatchTypes().add(Objects.requireNonNull(cType, "Catch type "+catchTypeId+" for trapline "+t.getId()+" does not exist!"));
		}
		if (pLine.getLastUpdated() != null) {
			t.setLastUpdated(LocalDateTime.parse(pLine.getLastUpdated()));
		}
		addTrapline(t);
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
    
    public boolean isLoading () {
    	return loadingProperty.get();
    }
    
    public ReadOnlyBooleanProperty loadingProperty () {
    	return loadingProperty.getReadOnlyProperty();
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
    	refreshCatchTypes();//Fetch the list of possible catch types
    	
    	RestClient traplineClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/trapline").header("Session-Token", loginService.getSessionToken());
    	
    	RestDataSource dataSource = traplineClient.createRestDataSource();
    	
    	BackgroundTasks.runInBackground(() -> {
    		try (JsonReader reader = JsonUtil.createJsonReader(dataSource.getInputStream())) {
    			JsonArray array = reader.readArray();
    			LOG.log(Level.INFO, "Response: "+array.toString());
    			try {
					//Wait for regions & catch types to load
    				appDataLoading.acquire(2);
				} catch (InterruptedException e) {
					//Silently ignore the interrupt
				}
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
        				trapline.getCatchTypes().clear();
        				
        				CatchType ct;
        				ct = catchTypes.getData().get(traplineJson.getInt("common_ct_id_1"));
        				if (ct != null) {
        					trapline.getCatchTypes().add(ct);
        				}
        				ct = catchTypes.getData().get(traplineJson.getInt("common_ct_id_2"));
        				if (ct != null) {
        					trapline.getCatchTypes().add(ct);
        				}
        				ct = catchTypes.getData().get(traplineJson.getInt("common_ct_id_3"));
        				if (ct != null) {
        					trapline.getCatchTypes().add(ct);
        				}
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
        				double latitude = trapJson.getJsonNumber("coord_lat").doubleValue();
        				double longitude = trapJson.getJsonNumber("coord_long").doubleValue();
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
    				trapline.setLastUpdated(LocalDateTime.now());
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
    			});
    		} catch (IOException | RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Problem requesting regions. Response: "+dataSource.getResponseMessage(), ex);
			} finally {
				//Signal region data has been fetched
				appDataLoading.release();
			}
    	});
    }
    
    protected void refreshCatchTypes () {
    	RestClient regionClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/catch-type").header("Session-Token", loginService.getSessionToken());
    	
    	RestDataSource dataSource = regionClient.createRestDataSource();
    	
    	if (catchTypes.getData() == null) {
    		catchTypes.setData(new HashMap<>());
    	}
    	
    	BackgroundTasks.runInBackground(() -> {
    		try (JsonReader reader = JsonUtil.createJsonReader(dataSource.getInputStream())) {
    			JsonArray array = reader.readArray();
    			LOG.log(Level.INFO, "Response: "+array.toString());
    			Platform.runLater(() -> {
    				for (JsonValue value : array) {
        				JsonObject regionJson = (JsonObject) value;
        				int id = regionJson.getInt("id");
        				CatchType catchType = catchTypes.getData().get(id);
        				if (catchType == null) {
        					catchType = new CatchType(id);
        					catchTypes.getData().put(id, catchType);
        				}
        				catchType.setName(regionJson.getString("name"));
        			}
    				catchTypes.setLastServerFetch(LocalDateTime.now());
    				cachingService.storeCatchTypes(catchTypes);
    			});
    		} catch (IOException | RuntimeException ex) {
    			LOG.log(Level.SEVERE, "Problem requesting catch types. Response: "+dataSource.getResponseMessage(), ex);
			} finally {
				//Signal catch type data has been fetched
				appDataLoading.release();
			}
    	});
    }

	public final ObservableList<Trapline> getTraplines() {
		return traplines;
	}	
	
	public final Map<Integer, CatchType> getCatchTypes() {
		return catchTypes.getData();
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
	    			cachingService.storeTrapline(t);
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
		LOG.log(Level.FINE, "Received change: "+c);
		while (c.next()) {
			if (c.wasAdded()) {
				for (Trapline t : c.getAddedSubList()) {
					updatedTraplines.add(t);
					t.getTraps().addListener(apiUpdateMonitor);
				}
			} else if (c.wasUpdated()) {
				for (Trapline t : c.getList().subList(c.getFrom(), c.getTo())) {
					updatedTraplines.add(t);
				}
			} else if (c.wasRemoved()) {
				for (Trapline t : c.getRemoved()) {
					t.getTraps().removeListener(apiUpdateMonitor);
				}
			}
		}
	}
}
