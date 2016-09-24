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
import org.nestnz.app.model.Trap;

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
	
	@Test
	public void testAddTrapPositions() {
		Trap t1 = new Trap(1, -40.82365, 170.27632);
		Trap t2 = new Trap(2, -40.82765, 170.27676);
		layer.getTraps().addAll(t1, t2);
		
		assertEquals(2, layer.points.size());//Make sure there's two points on the map
		
		MapPoint point = layer.points.get(0).getKey();//Check the first trap
		assertEquals(t1.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(t1.getLongitude(), point.getLongitude(), 0.0001);
		
		point = layer.points.get(1).getKey();//Check the second trap
		assertEquals(t2.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(t2.getLongitude(), point.getLongitude(), 0.0001);
	}
	
	@Test
	public void testActiveTrap() {
		Trap t1 = new Trap(1, -40.82365, 170.27632);
		Trap t2 = new Trap(2, -40.82765, 170.27676);
		layer.getTraps().addAll(t1, t2);
		
		assumeTrue(layer.points.size() == 2);//Assume there's two points on the map
		
		layer.setActiveTrap(t1);
		
		assertEquals(2, layer.points.size());//Make sure there's still only two points on the map
		
		MapPoint point = layer.points.get(1).getKey();//Check the position of the active trap
		assertEquals(t1.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(t1.getLongitude(), point.getLongitude(), 0.0001);
		
		assertEquals(layer.activeTrapIcon, layer.points.get(1).getValue());//Make sure the active trap icon is used
	}
	
	@Test
	public void testChangeActiveTrap() {
		Trap t1 = new Trap(1, -40.82365, 170.27632);
		Trap t2 = new Trap(2, -40.82765, 170.27676);
		layer.getTraps().addAll(t1, t2);
		
		layer.setActiveTrap(t1);
		
		assumeTrue(layer.points.size() == 2);//Assume there's only two points on the map
		
		layer.setActiveTrap(t2);
		
		assertEquals(2, layer.points.size());//Make sure there's still two points on the map (i.e. the old active trap is still on the map)
				
		MapPoint point = layer.points.get(1).getKey();//Make sure the active trap is the second trap
		assertEquals(t2.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(t2.getLongitude(), point.getLongitude(), 0.0001);		
		assertEquals(layer.activeTrapIcon, layer.points.get(1).getValue());//Make sure the active trap icon is used
		
		point = layer.points.get(0).getKey();//Make sure the inactive trap is still displayed
		assertEquals(t1.getLatitude(), point.getLatitude(), 0.0001);
		assertEquals(t1.getLongitude(), point.getLongitude(), 0.0001);		
		assertEquals(layer.trapIcons.get(t1), layer.points.get(0).getValue());//Make sure the inactive trap icon is used
		
	}

}
