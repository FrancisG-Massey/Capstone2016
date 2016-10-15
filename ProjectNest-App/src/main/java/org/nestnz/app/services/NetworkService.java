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
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;

import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * The NetworkService is used to communicate data between the app and the back-end API. 
 * Most implementations of NetworkService are asynchronous, and therefore return a status property which will be updated when they complete (either successfully or unsuccessfully).
 */
public interface NetworkService {
	
	public static enum RequestStatus { PENDING, SUCCESS, FAILED };

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
}
