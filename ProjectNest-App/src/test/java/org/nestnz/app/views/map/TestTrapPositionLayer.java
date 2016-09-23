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
package org.nestnz.app.views.map;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gluonhq.charm.down.common.Position;
import com.gluonhq.maps.MapPoint;

public class TestTrapPositionLayer {
	
	TrapPositionLayer layer;

	@Before
	public void setUp() throws Exception {
		layer = new TrapPositionLayer();		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testSetCurrentPosition() {
		Position pos = new Position(24.0943, 93.843);
		layer.setCurrentPosition(pos);
		MapPoint point = layer.points.get(0).getKey();
		assertEquals(pos.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(pos.getLongitude(), point.getLongitude(), 0.0001);
	}

	@Test
	public void testChangeCurrentPosition() {
		Position pos = new Position(24.0943, 93.843);
		layer.setCurrentPosition(pos);
		
		assumeTrue(layer.points.size() == 1);
		
		pos = new Position(25.0943, 94.832);
		layer.setCurrentPosition(pos);
		
		assertEquals(1, layer.points.size());//Make sure there's only one point on the map (i.e. the old position label was removed)
		
		MapPoint point = layer.points.get(0).getKey();
		
		assertEquals(pos.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(pos.getLongitude(), point.getLongitude(), 0.0001);
	}

}
