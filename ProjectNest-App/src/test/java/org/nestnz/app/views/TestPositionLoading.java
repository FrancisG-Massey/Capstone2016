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
package org.nestnz.app.views;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nestnz.app.services.TrapDataService;

import com.gluonhq.charm.down.ServiceFactory;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.PositionService;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TestPositionLoading {
	
	@SuppressWarnings("unchecked")
	static ServiceFactory<PositionService> posServiceFactory = mock(ServiceFactory.class);
	
	static PositionService posService = mock(PositionService.class);
	
	@BeforeClass
	public static void initPositionService() {
		when(posServiceFactory.getServiceType()).thenReturn(PositionService.class);
		when(posServiceFactory.getInstance()).thenReturn(Optional.of(posService));
		Services.registerServiceFactory(posServiceFactory);
	}
	
	
	NavigationView navView;
	
	TrapDataService dataService = mock(TrapDataService.class);
	
	
	
	ObjectProperty<Position> pos = new SimpleObjectProperty<>();

	@Before
	public void setUp() throws Exception {
		navView = new NavigationView(dataService, true);
		
		when(posService.positionProperty()).thenReturn(pos);
		when(posService.getPosition()).then(new Answer<Position>() {
			@Override
			public Position answer(InvocationOnMock invocation) throws Throwable {
				return pos.get();
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		navView = null;
	}

	@Test
	public void testCurrentPosBinding() {
		navView.initMonitors();
		
		Position testPos = new Position(83.9, 34.7);
		pos.set(testPos);
		assertEquals(testPos, navView.trapPositionLayer.getCurrentPosition());
	}

	/**
	 * Make sure the map is visible if a position is set
	 */
	@Test
	public void testShowingMap() {
		navView.initMonitors();
		
		Position testPos = new Position(88.9, 37.7);
		pos.set(testPos);
		assertEquals(navView.map, navView.getCenter());
	}

	/**
	 * Make sure the map is still showing if an initial position was set <i>before</i> the monitors were initialised
	 */
	@Test
	public void testShowingMapWithInitialPosition() {
		Position testPos = new Position(56.979, 76.675);
		pos.set(testPos);
		
		navView.initMonitors();
		
		assertEquals(navView.map, navView.getCenter());
	}

	/**
	 * Make sure no map is displayed if position is unavailable
	 */
	@Test
	public void testWaitingForPosition() {		
		navView.initMonitors();
		
		assertNotEquals(navView.map, navView.getCenter());
	}

}
