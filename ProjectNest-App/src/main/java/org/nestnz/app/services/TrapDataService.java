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

import java.util.Map;

import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.NetworkService.RequestStatus;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;


public interface TrapDataService {

	boolean isNetworkAvailable();

	boolean isLoading();

	ReadOnlyBooleanProperty loadingProperty();

	/**
	 * Fetches a trapline based on its ID. 
	 * This method only checks the memory cache for traplines - it doesn't check whether a trapline has been cached on the disk or exists on the server.
	 * @param id The ID of the trapline to lookup
	 * @return The trapline with the matching ID, or null if no trapline could be found
	 */
	Trapline getTrapline(int id);

	/**
	 * Requests an update for trapline metadata for all traplines this user can access.
	 * Note: This only updates the trapline metadata (name, ID, region, etc) - NOT the catch data or the traps themselves.
	 * This method is generally used to add traplines the user can now access, or remove those they can no longer access
	 */
	ReadOnlyObjectProperty<RequestStatus> refreshTraplines();

	/**
	 * Requests an update for all traps in the specified trapline
	 * Removes any traplines which no longer exist on the server (i.e. traps which aren't in the list of returned traps & don't have an id of zero)
	 * @param trapline The trapline to update
	 * @return A status property which changes to either SUCCESS or FAILED when the request completes 
	 */
	ReadOnlyObjectProperty<RequestStatus> refreshTrapline(Trapline trapline);

	/**
	 * Gets the list of traplines which the current user has access to.
	 * This list will be automatically updated when the trapline list is refreshed via {@link #refreshTraplines()}
	 * @return An {@link ObservableList} containing all traplines the user has access to
	 */
	ObservableList<Trapline> getTraplines();

	/**
	 * Gets all the catch types currently known by the app
	 * @return A lookup map linking catch type IDs with their respective catch types
	 */
	Map<Integer, CatchType> getCatchTypes();

	TraplineMonitorService getTraplineUpdateService(Trapline trapline);

}