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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.util.BackgroundTasks;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 * 
 */
public class GluonMapLoadingService implements MapLoadingService {

    private static final Logger LOG = Logger.getLogger(GluonMapLoadingService.class.getName());
    
    private static final String TILE_HOST = "http://tile.openstreetmap.org/";
	
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
		long minX = getTileX(minLongitude, ZOOM);
		long minY = getTileY(minLatitude, ZOOM);
		long maxX = getTileX(maxLongitude, ZOOM);
		long maxY = getTileY(maxLatitude, ZOOM);
		
		LOG.log(Level.INFO, minX+", "+minY+" to "+maxX+", "+maxY+" (xRange="+(maxX-minX+1)+", yRange="+(maxY-minY+1)+")");
		
		long result = (maxX-minX+1)*(maxY-minY+1);
		return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
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
	public ReadOnlyIntegerProperty preloadMapTiles(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		ReadOnlyIntegerWrapper remainingCount = new ReadOnlyIntegerWrapper();
		int minX = getTileX(minLongitude, ZOOM);
		int maxX = getTileX(maxLongitude, ZOOM);
		int minY = getTileY(minLatitude, ZOOM);
		int maxY = getTileY(maxLatitude, ZOOM);
		int totalCount = (maxX-minX+1)*(maxY-minY+1);
		if (totalCount > MAX_TILES) {
			throw new IllegalArgumentException("The number of tiles required ("+totalCount+") is greater than the maximum allowed ("+MAX_TILES+")");
		}
		int remaining = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				File f = getCacheFile(ZOOM, x, y);
				if (!f.exists()) {
					remaining++;
					fetchAndStoreTile(remainingCount, ZOOM, x, y);
				}
			}
		}
		remainingCount.set(remaining);
		return remainingCount;
	}
	
	protected File getCacheFile (int zoom, int x, int y) {
		return new File(cacheRoot, zoom + File.separator + x + File.separator + y + ".png");
	}
	
	private void fetchAndStoreTile(ReadOnlyIntegerWrapper remaining, int zoom, int x, int y) {
		BackgroundTasks.runInBackground(() -> {
			try {
	        	String urlString = TILE_HOST+zoom+"/"+x+"/"+y+".png";
	            URL url = new URL(urlString);
	            try (InputStream inputStream = url.openConnection().getInputStream()) {
	                File candidate = getCacheFile(zoom, x, y);
	                candidate.getParentFile().mkdirs();
	                try (FileOutputStream fos = new FileOutputStream(candidate)) {
	                    byte[] buff = new byte[4096];
	                    int len = inputStream.read(buff);
	                    while (len > 0) {
	                        fos.write(buff, 0, len);
	                        len = inputStream.read(buff);
	                    }
	                    fos.close();
	                }
	            }
	        } catch (IOException ex) {
	            LOG.log(Level.SEVERE, "Failed to fetch & store map tile "+x+","+y, ex);
	        } finally {
	        	Platform.runLater(() -> {
	        		remaining.set(remaining.get()-1);//Identify it as loaded regardless
	        	});	        	
	        }
		});        
    }

}
