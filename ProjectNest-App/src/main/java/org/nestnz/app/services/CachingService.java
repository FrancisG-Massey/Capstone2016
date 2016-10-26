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
import java.util.function.Consumer;

import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.Cacheable;
import org.nestnz.app.parser.ParserCatchTypeList;
import org.nestnz.app.parser.ParserTrapline;

import com.gluonhq.connect.GluonObservableObject;

/**
 * This service is used to monitor traplines for changes & save them in a cache, so if the app crashes/looses power/whatever, the data is not lost.
 * The service also loads the cached data from disk on application startup
 */
public interface CachingService {

	/**
	 * Loads all the traplines currently in the cache. 
	 * For every trapline found, {@code callback} is called with the trapline as an argument
	 * @param callback The function to call for each trapline in the cache
	 */
	void fetchTraplines(Consumer<ParserTrapline> callback);

	/**
	 * Updates the cached version of the trapline and flags the trapline as dirty
	 * @param trapline The trapline to update
	 * @return a {@link GluonObservableObject} which is set when the object is fully written
	 */
	GluonObservableObject<ParserTrapline> storeTrapline(Trapline trapline);
	
	/**
	 * Removes a trapline from the cache, if it exists.
	 * If no cached version exists, this method does nothing
	 * @param trapline The trapline to remove
	 */
	void removeTrapline (Trapline trapline);

	/**
	 * Loads the list of catch types currently stored in the cache
	 * If no catch types are stored, an empty list is returned
	 * @return A {@link GluonObservableObject}, which will contain the catch types when {@link GluonObservableObject#initializedProperty()} is set to true
	 */
	GluonObservableObject<Cacheable<Map<Integer, CatchType>>> fetchCatchTypes();

	GluonObservableObject<ParserCatchTypeList> storeCatchTypes(Cacheable<Map<Integer, CatchType>> catchTypes);

}