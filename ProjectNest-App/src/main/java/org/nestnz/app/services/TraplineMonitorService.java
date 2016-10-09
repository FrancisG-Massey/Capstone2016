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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;

import javafx.collections.ListChangeListener;

/**
 * This service is used to monitor traps in a trapline for changes (such as logged catches & changes to trap coords).
 * Whenever a trap is added or updated on the monitored list, the change is either sent directly to the API (if internet is available) or added to a queue to be sent to the server when it becomes available.
 */
public class TraplineMonitorService implements ListChangeListener<Trap> {

    private static final Logger LOG = Logger.getLogger(TraplineMonitorService.class.getName());
    
    private final Trapline trapline;
    
    private final NetworkService networkService;
    
    /**
     * Represents catches which have been sent to the network service to be logged in the server, to prevent duplicate requests
     * FIXME: At the moment catches added here are never removed - a possible memory leak. 
     */
    private final Set<Catch> pendingCatches = new HashSet<>();
    
    public TraplineMonitorService (Trapline trapline, NetworkService networkService) {
    	this.trapline = Objects.requireNonNull(trapline);
    	this.networkService = networkService;
    }
	
	/* (non-Javadoc)
	 * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
	 */
	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Trap> change) {
		while (change.next()) {
			if (change.wasUpdated()) {
				for (Trap trap : change.getList().subList(change.getFrom(), change.getTo())) {
					for (Catch c : trap.getCatches()) {
						if (!c.getId().isPresent() && !pendingCatches.contains(c)) {
							pendingCatches.add(c);
							networkService.sendLoggedCatch(trap.getId(), c);
							LOG.log(Level.INFO, String.format("Detected unsent catch log: %s", c));
						}
					}
				}
			} else if (change.wasAdded()) {
				for (Trap trap : change.getAddedSubList()) {
					networkService.sendCreatedTrap(trapline.getId(), trap);
					LOG.log(Level.INFO, String.format("Detected newly created trap: %s", trap));					
				}
			}
		}
	}

}
