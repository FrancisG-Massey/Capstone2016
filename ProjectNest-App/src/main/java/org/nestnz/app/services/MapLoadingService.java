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

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * The MapLoadingService is used to keep track of preloaded map tiles. 
 * NOTE: All methods here are based on tiles at the specified {@link #ZOOM}. This service will not work for any other zoom level. 
 */
public interface MapLoadingService {
	
	/**
	 * The fixed zoom level used for all map tile pre-loading.
	 * NOTE: If a map uses any zoom level other than this one, the tiles pre-loaded here will <em>NOT WORK</em>
	 */
	public static final int ZOOM = 17;
	
	/**
	 * The maximum number of tiles which can be preloaded via a call to {@link #preloadMapTiles(double, double, double, double)}
	 */
	public static final int MAX_TILES = 500;
	
	/**
	 * Finds out the total number of map tiles needed to fill the box which includes the provided coordinate ranges.
	 * The number returned by this method is the same number of tiles which will be loaded if {@link #preloadMapTiles(double, double, double, double)} is called with the same arguments.
	 * @param minLatitude The lowest latitude to include in the calculation
	 * @param maxLatitude The highest latitude to include in the calculation
	 * @param minLongitude The lowest longitude to include in the calculation
	 * @param maxLongitude The highest longitutde to include in the calculation
	 * @return The total number of map tiles needed to cover the given bounds
	 */
	public int getTotalTileCount (double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);
	
	/**
	 * Finds out the total number of map tiles currently cached which cover the given bounds
	 * If the number returned by this method matches the number returned by {@link #getTotalTileCount(double, double, double, double)}, the area is fully preloaded.
	 * @param minLatitude The lowest latitude to include in the calculation
	 * @param maxLatitude The highest latitude to include in the calculation
	 * @param minLongitude The lowest longitude to include in the calculation
	 * @param maxLongitude The highest longitutde to include in the calculation
	 * @return The number of map tiles currently cached covering the given boundaries
	 */
	public int getCachedTileCount (double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);
	
	/**
	 * Downloads all the map tiles within the provided bounds, and stores them in the map cache.
	 * This method could take a lot of time to complete (depending on the number of map tiles) and could use a lot of bandwidth, so this method should only be used for small areas.
	 * @param minLatitude The lowest latitude to include in the preloaded tiles
	 * @param maxLatitude The highest latitude to include in the preloaded tiles
	 * @param minLongitude The lowest longitude to include in the preloaded tiles
	 * @param maxLongitude The highest longitude to include in the preloaded tiles
	 * @return A property indicating the number of remaining map tiles to fetch. When this reaches zero, the map is fully pre-loaded
	 */
	public ReadOnlyIntegerProperty preloadMapTiles (double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);

}
