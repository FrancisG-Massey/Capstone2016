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
package org.nestnz.app.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.MapLoadingService;

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * 
 */
public class GluonMapLoadingService implements MapLoadingService {

    private static final Logger LOG = Logger.getLogger(GluonMapLoadingService.class.getName());
	
	private static int getTileX (final double lon, final int zoom) {
		int xtile = (int) Math.floor((lon + 180) / 360 * (1<<zoom));
		if (xtile < 0) {
			xtile = 0;
		}
		if (xtile >= (1<<zoom)) {
			xtile = (1<<zoom)-1;
		}
		return xtile;
	}
	
	private static int getTileY (final double lat, final int zoom) {
		int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom));
		if (ytile < 0) {
			ytile = 0;
		}
		if (ytile >= (1<<zoom)) {
			ytile = (1<<zoom)-1;
		}
		return ytile;
	}
	
	/**
	 * Converts the provided latitude, longitude, and zoom into an OpenStreetMap Slippy tile number
	 * @param lat The desired latitude of the tile number
	 * @param lon The desired longitude of the tile number
	 * @param zoom The desired zoom level for the tile
	 * @return The tile number path (to be appended to the URL when requesting a tile)
	 * @see http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 */
	public static String getTileNumber(final double lat, final double lon, final int zoom) {
		int xtile = getTileX(lon, zoom);
		int ytile = getTileY(lat, zoom);		
		return "" + zoom + "/" + xtile + "/" + ytile;
	}
	
	
	private File cacheRoot;
	
	public GluonMapLoadingService (File storageRoot) throws IOException {
        cacheRoot = new File(storageRoot, ".gluonmaps");
        if (!cacheRoot.exists()) {
        	cacheRoot.mkdirs();
        }
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.MapLoadingService#getTotalTileCount(double, double, double, double)
	 */
	@Override
	public int getTotalTileCount(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		int minX = getTileX(minLongitude, ZOOM);
		int minY = getTileY(minLatitude, ZOOM);
		int maxX = getTileX(maxLongitude, ZOOM);
		int maxY = getTileY(maxLatitude, ZOOM);
		return (maxX-minX+1)*(maxY-minY+1);
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.MapLoadingService#getCachedTileCount(double, double, double, double)
	 */
	@Override
	public int getCachedTileCount(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		int sum = 0;
		int minX = getTileX(minLongitude, ZOOM);
		int maxX = getTileX(maxLongitude, ZOOM);
		int minY = getTileY(minLatitude, ZOOM);
		int maxY = getTileY(maxLatitude, ZOOM);
		LOG.log(Level.FINE, "Searching from "+minX+", "+minY+" to "+maxX+", "+maxY);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				File f = getCacheFile(ZOOM, x, y);
				if (f.exists()) {
					sum++;
				}
			}
		}
		return sum;
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.MapLoadingService#preloadMapTiles(double, double, double, double)
	 */
	@Override
	public ReadOnlyIntegerProperty preloadMapTiles(double minLatitude, double maxLatitude, double minLongitude,
			double maxLongitude) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected File getCacheFile (int zoom, int x, int y) {
		return new File(cacheRoot, zoom + File.separator + x + File.separator + y + ".png");
	}

}
