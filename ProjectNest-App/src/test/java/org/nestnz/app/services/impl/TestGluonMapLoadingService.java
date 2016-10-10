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

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nestnz.app.services.MapLoadingService;

/**
 * 
 */
public class TestGluonMapLoadingService {
	
	MapLoadingService mapService;
	
	Path cachePath;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cachePath = Files.createTempDirectory("trapDataCache");
		mapService = new GluonMapLoadingService(cachePath.toFile());
		
		cachePath = cachePath.resolve(".gluonmaps");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		mapService = null;
	}

	@Test
	public void testGetTileCount() {		
		double minLat = -40.309037;//81598
		double minLong = 175.772448;//129532
		double maxLat = -40.314206;//81600
		double maxLong = 175.779946;//129535
		
		int count = mapService.getTotalTileCount(minLat, maxLat, minLong, maxLong);
		
		int oracle = 3 * 4;//Inclusive difference of 3 tiles along the y-axis and 4 tiles along the x-axis
		
		assertEquals(oracle, count);
	}

	@Test
	public void testGetCachedTileCount() throws IOException {
		String[] files = {
				//These files should be counted
				"17/129532/81598.png",
				"17/129532/81599.png",
				"17/129535/81600.png",
				
				//Throw in some edge case files to make sure it's not accidently counting them
				"17/129536/81600.png",
				"17/129535/81601.png",
				"17/129531/81600.png",
				"18/129535/81600.png"
		};
		for (String file : files) {
			Path p = cachePath.resolve(file);
			Files.createDirectories(p.getParent());
			Files.createFile(p);
		}

		double minLat = -40.309037;//81598
		double minLong = 175.772448;//129532
		double maxLat = -40.314206;//81600
		double maxLong = 175.779946;//129535
		
		int count = mapService.getCachedTileCount(minLat, maxLat, minLong, maxLong);
		
		
		assertEquals(3, count);
	}

}
