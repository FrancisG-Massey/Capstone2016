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
import org.nestnz.app.services.NetworkService.RequestStatus;
import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.connect.GluonObservableObject;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public final class TrapDataService implements ListChangeListener<Trapline> {

    private static final Logger LOG = Logger.getLogger(TrapDataService.class.getName());
	
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList(trapline -> {
    	return new Observable[] { trapline.nameProperty(), trapline.regionProperty(), trapline.getTraps(),
    			trapline.endProperty(), trapline.startProperty(), trapline.getCatchTypes(),
    			trapline.lastUpdatedProperty() };
    });
    
    private final Map<Integer, Region> regions = new HashMap<>();
    
    private final Map<Trapline, TraplineMonitorService> apiUpdateMonitors = new HashMap<>();
    
    private final CachingService cachingService;
    
    private final NetworkService networkService;
    
    private final ReadOnlyBooleanWrapper loadingProperty = new ReadOnlyBooleanWrapper(false);
    
    private final Cacheable<Map<Integer, CatchType>> catchTypes = new Cacheable<>();
    
    /**
     * A semaphore used to ensure regions & catch types have been loaded from the server before setting up traplines
     */
    private final Semaphore appDataLoading = new Semaphore(0);
    
    public TrapDataService (CachingService cachingService, NetworkService networkService) throws IOException {
    	this.cachingService = Objects.requireNonNull(cachingService);
    	this.networkService = networkService;
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
			if (pTrap.getCatches() != null) {
				for (ParserCatch pCatch : pTrap.getCatches()) {
					CatchType cType = catchTypes.getData().get(pCatch.getTypeId());			
					LocalDateTime timestamp = LocalDateTime.parse(pCatch.getTimestamp());
					trap.getCatches().add(new Catch(cType, timestamp));
				}
			} else {
				LOG.log(Level.WARNING, "getCatches() is returning null for trapline "+pLine.getId()+", trap "+pTrap.getId());
			}
			t.getTraps().add(trap);
		}
		if (pLine.getCatchTypes() != null) {
			for (long catchTypeId : pLine.getCatchTypes()) {
				CatchType cType = catchTypes.getData().get(Integer.valueOf((int) catchTypeId));			
				t.getCatchTypes().add(Objects.requireNonNull(cType, "Catch type "+catchTypeId+" for trapline "+t.getId()+" does not exist!"));
			}
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
    	
		BackgroundTasks.runInBackground(() -> {
			try {
				//Wait for regions & catch types to load
				appDataLoading.acquire(2);
			} catch (InterruptedException e) {
				//Silently ignore the interrupt
			}
			Set<Integer> validLineIds = new HashSet<>();
			
			networkService.loadTraplines(trapline -> { 	
				validLineIds.add(trapline.getId());
				
	    		Trapline oldTrapline = getTrapline(trapline.getId());
	    		if (oldTrapline == null) {
	    			populateTrapline(trapline, trapline);
	    			traplines.add(trapline);
	    		} else {
	    			populateTrapline(oldTrapline, trapline);
	    		}
	    	}).addListener((obs, oldStatus, newStatus) -> {
	    		switch(newStatus) {
				case SUCCESS:
					//Remove any traplines which no longer exist
					Iterator<Trapline> iterator = traplines.iterator();
    				while (iterator.hasNext()) {
    					Trapline line = iterator.next();
    					if (!validLineIds.contains(line.getId())) {
    						iterator.remove();
    					}
    				}
					//Fall through
				case FAILED_UNAUTHORISED:
				case FAILED_NETWORK:
				case FAILED_OTHER:
					loadingProperty.set(false);//Signal loading is complete
					break;
				case PENDING:
					break;
	    		}
	    	});
		});
    }
    
    private void populateTrapline (Trapline output, Trapline input) {
    	Region r = regions.get(input.getRegion().getId());
    	output.setRegion(Objects.requireNonNull(r, "Invalid region: "+input.getRegion().getId()));
    	CatchType ct1 = input.getCatchTypes().get(0);
    	CatchType ct2 = input.getCatchTypes().get(1);
    	CatchType ct3 = input.getCatchTypes().get(2);
    	
    	output.getCatchTypes().clear();

    	Map<Integer, CatchType> catchTypeCopy = new HashMap<>(catchTypes.getData());
		
		//Add the most common catch types first
		CatchType ct;
		ct = catchTypeCopy.remove(ct1.getId());
		if (ct != null) {
			output.getCatchTypes().add(ct);
		}
		ct = catchTypeCopy.remove(ct2.getId());
		if (ct != null) {
			output.getCatchTypes().add(ct);
		}
		ct = catchTypeCopy.remove(ct3.getId());
		if (ct != null) {
			output.getCatchTypes().add(ct);
		}
		
		//Then add whatever's left over
		output.getCatchTypes().addAll(catchTypeCopy.values());
    }
    
    public ReadOnlyObjectProperty<RequestStatus> loadTrapline (Trapline trapline) {
    	if (loadingProperty.get()) {
    		return null;
    	}
    	loadingProperty.set(true);
    	
		Set<Integer> validTrapIds = new HashSet<>();
		validTrapIds.add(0);//0 = trap not yet created on server
    	
		ReadOnlyObjectProperty<RequestStatus> status = networkService.loadTrapline(trapline, trap -> {
			validTrapIds.add(trap.getId());
			
    		Trap oldTrap = trapline.getTrap(trap.getId());
    		if (oldTrap == null) {
    			trapline.getTraps().add(trap);
    		} else {
    			oldTrap.setNumber(trap.getNumber());
    			oldTrap.setLatitude(trap.getLatitude());
    			oldTrap.setLongitude(trap.getLongitude());
    			oldTrap.setLastReset(trap.getLastReset());
    			oldTrap.setStatus(trap.getStatus());
    		}
    	});
		
		status.addListener((obs, oldStatus, newStatus) -> {
    		switch(newStatus) {
			case SUCCESS:				
				Iterator<Trap> iterator = trapline.getTraps().iterator();
				while (iterator.hasNext()) {
					Trap trap = iterator.next();
					if (!validTrapIds.contains(trap.getId())) {
						iterator.remove();
					}
				}
				trapline.setLastUpdated(LocalDateTime.now());
				
				//Fall through
			case FAILED_UNAUTHORISED:
			case FAILED_NETWORK:
			case FAILED_OTHER:
				loadingProperty.set(false);//Signal loading is complete
				break;
			case PENDING:
				break;
    		}
    	});
		
		return status;
    }
    
    protected void refreshRegions () {
    	networkService.loadRegions(region -> {
    		if (regions.containsKey(region.getId())) {
    			regions.get(region.getId()).setName(region.getName());
			} else {
				regions.put(region.getId(), region);				
			}
    	}).addListener((obs, oldStatus, newStatus) -> {
    		switch(newStatus) {
			case SUCCESS:
				//Fall through
			case FAILED_UNAUTHORISED:
			case FAILED_NETWORK:
			case FAILED_OTHER:
				//Signal region data has been fetched
				appDataLoading.release();
				break;
			case PENDING:
				break;
    		}
    	});
    }
    
    protected void refreshCatchTypes () {    	
    	if (catchTypes.getData() == null) {
    		catchTypes.setData(new HashMap<>());
    	}
    	
    	networkService.loadCatchTypes(catchType -> {
    		if (catchTypes.getData().containsKey(catchType.getId())) {
    			CatchType oldType = catchTypes.getData().get(catchType.getId());
    			oldType.setName(catchType.getName());
    			oldType.setImage(catchType.getImage());
			} else {
				catchTypes.getData().put(catchType.getId(), catchType);				
			}
    	}).addListener((obs, oldStatus, newStatus) -> {
    		switch(newStatus) {
			case SUCCESS:
				catchTypes.setLastServerFetch(LocalDateTime.now());
				cachingService.storeCatchTypes(catchTypes);
				//Fall through
			case FAILED_UNAUTHORISED:
			case FAILED_NETWORK:
			case FAILED_OTHER:
				//Signal catch type data has been fetched
				appDataLoading.release();
				break;
			case PENDING:
				break;
    		}
    	});
    }

	public ObservableList<Trapline> getTraplines() {
		return traplines;
	}	
	
	public Map<Integer, CatchType> getCatchTypes() {
		return catchTypes.getData();
	}

	private final Set<Trapline> updatedTraplines = new HashSet<>();
	private final Set<Trapline> removedTraplines = new HashSet<>();
    
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
    		if (!removedTraplines.isEmpty()) {
	    		Set<Trapline> removedCopy = new HashSet<>(removedTraplines);
	    		removedTraplines.clear();
	    		
	    		for (Trapline t : removedCopy) {
	    			cachingService.removeTrapline(t);
	    		}

	    		LOG.log(Level.INFO, "Removed "+removedCopy.size()+" old traplines from the file cache.");
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
					TraplineMonitorService s = new TraplineMonitorService(t, networkService);
					t.getTraps().addListener(s);
					apiUpdateMonitors.put(t, s);
				}
			} else if (c.wasUpdated()) {
				for (Trapline t : c.getList().subList(c.getFrom(), c.getTo())) {
					updatedTraplines.add(t);
				}
			} else if (c.wasRemoved()) {
				for (Trapline t : c.getRemoved()) {
					if (apiUpdateMonitors.containsKey(t)) {
						TraplineMonitorService s = apiUpdateMonitors.remove(t);
						t.getTraps().removeListener(s);
						s.cleanup();
					}
					removedTraplines.add(t);
					updatedTraplines.remove(t);
				}
			}
		}
	}
}
