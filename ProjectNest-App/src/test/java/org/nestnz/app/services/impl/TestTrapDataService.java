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
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.CachingService;
import org.nestnz.app.services.NetworkService;
import org.nestnz.app.services.cache.model.Cacheable;
import org.nestnz.app.services.cache.model.ParserCatch;
import org.nestnz.app.services.cache.model.ParserRegion;
import org.nestnz.app.services.cache.model.ParserTrap;
import org.nestnz.app.services.cache.model.ParserTrapline;
import org.nestnz.app.services.impl.DefaultTrapDataService;

import com.gluonhq.connect.GluonObservableObject;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;


public class TestTrapDataService {
	
	@BeforeClass
    public static void initToolkit() throws InterruptedException {
		//Source: http://stackoverflow.com/questions/11273773/javafx-2-1-toolkit-not-initialized
		final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        if (!latch.await(5L, TimeUnit.SECONDS)) {
            throw new ExceptionInInitializerError();
        }
    }
	
	DefaultTrapDataService dataService;
	CachingService cachingService = mock(CachingService.class);
	NetworkService networkService = mock(NetworkService.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dataService = new DefaultTrapDataService(cachingService, networkService);
		
		GluonObservableObject<Cacheable<Map<Integer, CatchType>>> results = new GluonObservableObject<>();	    
		Cacheable<Map<Integer, CatchType>> catchTypes = new Cacheable<>();    	
    	catchTypes.setData(new HashMap<>());
		results.setValue(catchTypes);
        ((SimpleBooleanProperty) results.initializedProperty()).set(true);
		when(cachingService.fetchCatchTypes()).thenReturn(results);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		dataService = null;
	}
	
	@Test
	public void testTraplineLoading () throws Exception {
		doAnswer(new Answer<Void> () {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Consumer<ParserTrapline> callback = invocation.getArgument(0);
				List<ParserTrap> traps = new ArrayList<>();
				
				List<ParserCatch> catches = new ArrayList<>();
				catches.add(new ParserCatch(36, 10_000, "2016-08-13T10:30:17", "Test Note"));
				
				traps.add(new ParserTrap(7623, 1, -40.314206, 175.779946, "ACTIVE", "2016-04-16T10:26:07", "2016-08-16T10:28:07", catches));
				traps.add(new ParserTrap(7468, 2, -40.311086, 175.775306, "ACTIVE", "2016-04-16T10:30:07", "2016-08-16T10:30:07", new ArrayList<>()));
				traps.add(new ParserTrap(9072, 3, -40.311821, 175.775993, "INACTIVE", "2016-04-16T10:36:07", "2016-08-16T10:36:07", null));
				
				List<Long> catchTypes = new ArrayList<>();
				
				callback.accept(new ParserTrapline(20, "Test trapline", traps, "Test Start", null, 
						new ParserRegion(20, "Test Region"), catchTypes, "2016-09-10T15:34:07", true));
				return null;
			}			
		}).when(cachingService).fetchTraplines(any());
		
		dataService.getCatchTypes().put(10_000, CatchType.OTHER);
				
		//Since traplines are loaded asynchronosly, we need to add a listener & wait for them to load
		CompletableFuture<Trapline> future = new CompletableFuture<Trapline>();
		dataService.getTraplines().addListener((ListChangeListener<Trapline>)change -> {
			change.next();
			if (change.wasAdded()) {
				future.complete(change.getAddedSubList().get(0));
			} else {
				fail("Invalid change type: "+change);
			}
		});
		
		dataService.initTraplines();
		
		Trapline trapline = future.get(2, TimeUnit.SECONDS);//Wait 2 seconds at most
		
		Trapline oracle = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
				
		//Check if the trapline itself is equal to the expected trapline
		assertEquals(oracle, trapline);
		
		//Make sure the 'canEdit' property was set to true
		assertTrue(trapline.canEdit());
		
		//Check the last server update timestamp was set correctly
		assertEquals(LocalDateTime.parse("2016-09-10T15:34:07"), trapline.getLastUpdated().get());
		
		//Make sure the trapline has 3 traps
		assertEquals(3, trapline.getTraps().size());
		
		Trap oracleTrap = new Trap(7468, 2, -40.311086, 175.775306, TrapStatus.ACTIVE, LocalDateTime.parse("2016-04-16T10:30:07"), LocalDateTime.parse("2016-08-16T10:30:07"));
		
		//Check one of the traps to ensure it loaded correctly
		assertEquals(oracleTrap, trapline.getTraps().get(1));    	
		
		Catch oracleCatch = new Catch(CatchType.OTHER, LocalDateTime.parse("2016-08-13T10:30:17"), "Test Note");
		oracleCatch.setId(36);
		
		assertEquals(oracleCatch, trapline.getTraps().get(0).getCatches().get(0));
	}

}
