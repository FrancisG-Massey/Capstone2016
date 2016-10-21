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

import java.util.function.Consumer;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * The NetworkService is used to communicate data between the app and the back-end API. 
 * Most implementations of NetworkService are asynchronous, and therefore return a status property which will be updated when they complete (either successfully or unsuccessfully).
 */
public interface NetworkService {
	
	public static enum RequestStatus { 
		/**
		 * The request is currently waiting for a response from the server
		 */
		PENDING, 
		
		/**
		 * The request completed successfully
		 */
		SUCCESS, 
		
		/**
		 * The request failed because the user isn't authorised to read from/write to the specified resource
		 */
		FAILED_UNAUTHORISED, 
		
		/**
		 * The request failed because the network isn't available at the moment
		 */
		FAILED_NETWORK,
		
		/**
		 * The request failed for some other reason
		 */
		FAILED_OTHER 
	};
	
	/**
	 * Checks whether the network has been marked as 'available'
	 * If the network is not marked as available, all automated network requests should be stopped - only requests initiated by the user (e.g. pressing the 'refresh' button) should be processed.
	 * @return True if the network has been marked as available, false otherwise
	 */
	public boolean isNetworkAvailable();
	
	/**
	 * The networkAvailableProperty indicates whether previous network requests have failed due to a network error (i.e. haven't received any http response).
	 * If the property is set to false and a network request completes successfully, the property will be set to true.
	 * If set to false, automatic network requests (such as those performed when a view is opened, catch is logged, etc) should not be completed - only those manually initiated by the user
	 * @return A {@link ReadOnlyBooleanProperty} which can be listened on to detect when the network becomes available/unavailable
	 */
	public ReadOnlyBooleanProperty networkAvailableProperty();

	/**
	 * Submits a logged catch in a trap to the API. 
	 * After the request completes successfully, {@link Catch#idProperty()} will be set to the server id of the newly created catch log
	 * @param trapId The ID of the trap in which this catch was logged
	 * @param loggedCatch The information about the newly logged catch
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> sendLoggedCatch(int trapId, Catch loggedCatch);
	
	/**
	 * Submits a new trap within a trapline to the API. 
	 * After the request completes successfully, {@link Trap#idProperty()} will be set to the server id of the newly created trap
	 * @param traplineId The ID of the trapline in which this trap was created
	 * @param trap The information of the newly created trap.
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> sendCreatedTrap(int traplineId, Trap trap);
	
	/**
	 * Fetches the list of regions from the API. For each region returned, {@code loadCallback} is called.
	 * @param loadCallback The function to call for each region fetched from the server
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> loadRegions(Consumer<Region> loadCallback);
	
	/**
	 * Fetches the list of catch types from the API. For each region returned, {@code loadCallback} is called.
	 * @param loadCallback The function to call for each catch type fetched from the server
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> loadCatchTypes(Consumer<CatchType> loadCallback);
	
	/**
	 * Fetches all traps belonging to the specified trapline from the API.
	 * For each trap returned, {@code loadCallback} is called.
	 * @param trapline The trapline for which to load traps
	 * @param loadCallback The function to call for each trap fetched from the server
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> loadTrapline(Trapline trapline, Consumer<Trap> loadCallback);
	
	/**
	 * Fetches all traplines for which the user has permission to access from the API.
	 * For each trapline returned, {@code loadCallback} is called.
	 * @param loadCallback The function to call for each trapline fetched from the server
	 * @return A status property which will change to SUCCESS or FAILED when the request completes
	 */
	public ReadOnlyObjectProperty<RequestStatus> loadTraplines(Consumer<Trapline> loadCallback);
}
