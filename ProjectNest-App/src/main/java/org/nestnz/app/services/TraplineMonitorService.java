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

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.NetworkService.RequestStatus;
import org.nestnz.app.util.BackgroundTasks;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.util.Pair;

/**
 * This service is used to monitor traps in a trapline for changes (such as logged catches & changes to trap coords).
 * Whenever a trap is added or updated on the monitored list, the change is either sent directly to the API (if internet is available) or added to a queue to be sent to the server when it becomes available.
 */
public class TraplineMonitorService implements ListChangeListener<Trap> {

    private static final Logger LOG = Logger.getLogger(TraplineMonitorService.class.getName());
    
    private final Trapline trapline;
    
    private final NetworkService networkService;
    
    private final ObservableSet<Trap> createdTraps = FXCollections.observableSet(new HashSet<>());
    
    /**
     * Represents catches which have been sent to the network service to be logged in the server, to prevent duplicate requests
     */
    private final ObservableSet<Pair<Integer, Catch>> loggedCatches = FXCollections.observableSet(new HashSet<>());
    
    private final ScheduledFuture<?> updateTask;
    
    public TraplineMonitorService (Trapline trapline, NetworkService networkService) {
    	this.trapline = Objects.requireNonNull(trapline);
    	this.networkService = networkService;
    	
    	for (Trap trap : trapline.getTraps()) {
    		if (trap.getId() == 0) {
    			createdTraps.add(trap);
				LOG.log(Level.INFO, String.format("Detected newly created trap: %s", trap));
    		} else {
				//Don't check for catches on a newly created trap, as we won't be able to send them to the server until we know their ID
	    		for (Catch c : trap.getCatches()) {
					if (!c.getId().isPresent() && !loggedCatches.contains(c)) {
						loggedCatches.add(new Pair<>(trap.getId(), c));
						LOG.log(Level.INFO, String.format("Detected unsent catch log: %s", c));
					}
				}
    		}
    	}
    	
    	this.updateTask = BackgroundTasks.scheduleRepeating(() -> {
    		if (networkService.isNetworkAvailable()) {
	    		sendCatchesToServer();
	    		sendTrapsToServer();
    		}
    	}, 30, TimeUnit.SECONDS);
    }
    
    public void cleanup () {
    	updateTask.cancel(false);
    }
    
    /**
     * Sends logged catches to the server
     * @return A {@link ReadOnlyObjectProperty} which is set to {@link RequestStatus#SUCCESS} if all catches were sent successfully, or to an error if any requests failed.
     */
    public ReadOnlyObjectProperty<RequestStatus> sendCatchesToServer () {
    	ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>();
    	if (loggedCatches.isEmpty()) {
    		//Run the status update later, so the calling method has a chance to register a listener first
    		Platform.runLater(() -> status.set(RequestStatus.SUCCESS));
    	} else {
    		Platform.runLater(() -> status.set(RequestStatus.PENDING));
    		IntegerProperty remaining = new SimpleIntegerProperty(loggedCatches.size());
    		remaining.addListener((obs, oldVal, newVal) -> {
    			if (newVal.intValue() == 0) {
    				status.set(RequestStatus.SUCCESS);
    			}
    		});
        	for (Pair<Integer, Catch> c : loggedCatches) {
				networkService.sendLoggedCatch(c.getKey(), c.getValue()).addListener((obs, oldStatus, newStatus) -> {
					switch (newStatus) {
					case PENDING:
						break;
					case SUCCESS:
						remaining.set(remaining.get()-1);
						loggedCatches.remove(c);
						break;
					case FAILED_OTHER:
						LOG.log(Level.WARNING, "Failed to send logged catch "+c+" (server responded with an error code) - removing from cache.");
						loggedCatches.remove(c);
						trapline.getTrap(c.getKey()).getCatches().remove(c.getValue());
						remaining.set(remaining.get()-1);
						break;
					case FAILED_NETWORK:
					case FAILED_UNAUTHORISED:
						status.set(newStatus);
						break;
					}
				});
	    	}
	    	LOG.log(Level.INFO, "Sent "+loggedCatches.size()+" logged catches to the server.");
    	}
    	return status.getReadOnlyProperty();
    }
    
    public ReadOnlyObjectProperty<RequestStatus> sendTrapsToServer () {
    	ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
    	if (createdTraps.isEmpty()) {
    		//Run the status update later, so the calling method has a chance to register a listener first
    		Platform.runLater(() -> status.set(RequestStatus.SUCCESS));
    	} else {
    		IntegerProperty remaining = new SimpleIntegerProperty(createdTraps.size());
    		remaining.addListener((obs, oldVal, newVal) -> {
    			if (newVal.intValue() == 0) {
    				status.set(RequestStatus.SUCCESS);
    			}
    		});
	    	for (Trap t : createdTraps) {
				networkService.sendCreatedTrap(trapline.getId(), t).addListener((obs, oldStatus, newStatus) -> {
					switch (newStatus) {
					case PENDING:
						break;
					case SUCCESS:
						remaining.set(remaining.get()-1);
						createdTraps.remove(t);
						break;
					case FAILED_OTHER:
						LOG.log(Level.WARNING, "Failed to send created trap "+t+" (server responded with an error code) - removing from cache.");
						createdTraps.remove(t);
						trapline.getTraps().remove(t);
						remaining.set(remaining.get()-1);
						break;
					case FAILED_NETWORK:
					case FAILED_UNAUTHORISED:
						status.set(newStatus);
						break;
					}
				});
	    	}
	    	LOG.log(Level.INFO, "Sent "+createdTraps.size()+" created traps to the server.");
    	}
    	return status.getReadOnlyProperty();
    }
    
    public ObservableSet<Trap> getUnsentTraps () {
    	return createdTraps;
    }
    
    public ObservableSet<Pair<Integer, Catch>> getUnsentCatchLogs () {
    	return loggedCatches;
    }
	
	/* (non-Javadoc)
	 * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
	 */
	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Trap> change) {
		while (change.next()) {
			if (change.wasUpdated()) {
				for (Trap trap : change.getList().subList(change.getFrom(), change.getTo())) {
					if (trap.getId() == 0) {
						createdTraps.add(trap);
						LOG.log(Level.INFO, String.format("Detected newly created trap: %s", trap));
					} else {
						//Don't check for catches on a newly created trap, as we won't be able to send them to the server until we know their ID
						for (Catch c : trap.getCatches()) {
							if (!c.getId().isPresent() && !loggedCatches.contains(c)) {
								loggedCatches.add(new Pair<>(trap.getId(), c));
								LOG.log(Level.INFO, String.format("Detected unsent catch log: %s", c));
							}
						}
					}
				}
			} else if (change.wasAdded()) {
				for (Trap trap : change.getAddedSubList()) {
					if (trap.getId() == 0) {//Only try to create traps if they haven't yet been created
						createdTraps.add(trap);
						LOG.log(Level.INFO, String.format("Detected newly created trap: %s", trap));
					}
				}
			}
		}
	}
}
