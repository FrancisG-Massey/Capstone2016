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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.Cacheable;
import org.nestnz.app.parser.ParserRegion;
import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.parser.ParserTrapline;
import org.nestnz.app.services.impl.DefaultCachingService;

import com.gluonhq.connect.GluonObservableObject;

import javafx.embed.swing.JFXPanel;

public class TestCachingService {

    private static final Logger LOG = Logger.getLogger(TestCachingService.class.getName());
	
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
	
	CachingService dataService;
	Path cachePath;

	@Before
	public void setUp() throws Exception {
		cachePath = Files.createTempDirectory("trapDataCache");
		dataService = new DefaultCachingService(cachePath.toFile());
		
		Path testData = Paths.get(TestCachingService.class.getResource("catchTypes.json").toURI());
		Files.copy(testData, cachePath.resolve("catchTypes.json"), StandardCopyOption.REPLACE_EXISTING);
		LOG.log(Level.INFO, String.format("Copied sample catch types from %s to %s", testData.toString(), cachePath.resolve("catchTypes.json").toString()));
		
		cachePath = cachePath.resolve("traplines");
    	
		dataService.fetchCatchTypes();
	}

	@After
	public void tearDown() throws Exception {
		dataService = null;
	}

	/**
	 * Verifies that a file is actually saved when the "update" method is called.
	 * Does not ensure the file content is valid (this is a separate test case)
	 */
	@Test
	public void testFileSaved() throws Exception {
    	assumeFalse(Files.exists(cachePath.resolve("20.json")));
		Trapline trapline = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
		
		updateAndBlock(trapline);
        
    	assertTrue(Files.exists(cachePath.resolve("20.json")));
	}
	
	@Test
	public void testFileLoading () throws Exception {
		Path testData = Paths.get(TestCachingService.class.getResource("20.json").toURI());
		Files.copy(testData, cachePath.resolve("20.json"), StandardCopyOption.REPLACE_EXISTING);
		LOG.log(Level.INFO, String.format("Moved test resource from %s to %s", testData.toString(), cachePath.resolve("20.json").toString()));
				
		//Since traplines are loaded asynchronosly, we need to add a listener & wait for them to load
		CompletableFuture<ParserTrapline> future = new CompletableFuture<ParserTrapline>();
		dataService.fetchTraplines(pTrapline -> future.complete(pTrapline));
		
		ParserTrapline trapline = future.get(2, TimeUnit.SECONDS);//Wait 2 seconds at most
		
		List<ParserTrap> traps = new ArrayList<>();
		
		traps.add(new ParserTrap(7623, 1, -40.314206, 175.779946, "ACTIVE", "2016-04-16T10:26:07", "2016-08-16T10:28:07", new ArrayList<>()));
		traps.add(new ParserTrap(7468, 2, -40.311086, 175.775306, "ACTIVE", "2016-04-16T10:30:07", "2016-08-16T10:30:07", new ArrayList<>()));
		traps.add(new ParserTrap(9072, 3, -40.311821, 175.775993, "INACTIVE", "2016-04-16T10:36:07", "2016-08-16T10:36:07", new ArrayList<>()));
		
		List<Long> catchTypes = new ArrayList<>();
		
		ParserTrapline oracle = new ParserTrapline(20, "Test trapline", traps, "Test Start", null, new ParserRegion(20, "Test Region"), catchTypes);
				
		//Check if the trapline itself is equal to the expected trapline
		assertEquals(oracle, trapline); 	
	}
	
	@Test
	public void testSaveAndLoad () throws Exception {
		Trapline trapline = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
		Trap trap1 = new Trap(49, 1, -40.314206, 175.779946, TrapStatus.ACTIVE, LocalDateTime.parse("2016-04-16T10:26:07"), LocalDateTime.parse("2016-08-16T10:28:07"));
		Trap trap2 = new Trap(76, 2, 4.7238, 50.8456, TrapStatus.INACTIVE, LocalDateTime.parse("2016-08-16T10:37:07.565"), null);
		trapline.getTraps().add(trap1);
		trapline.getTraps().add(trap2);

		updateAndBlock(trapline);
    	assumeTrue(Files.exists(cachePath.resolve("20.json")));
		
		//Since traplines are loaded asynchronosly, we need to add a listener & wait for them to load
		CompletableFuture<ParserTrapline> future = new CompletableFuture<ParserTrapline>();
		dataService.fetchTraplines(pTrapline -> future.complete(pTrapline));
		
		ParserTrapline result = future.get(2, TimeUnit.SECONDS);//Wait 2 seconds at most
		
		ParserTrapline oracle = new ParserTrapline(trapline);
		
		assertEquals(oracle, result);//Make sure the trapline itself remains the same
	}
	
	@Test
	public void testLoadCatchTypes () throws Exception {
		CompletableFuture<Map<Integer, CatchType>> future = new CompletableFuture<>();
		
		GluonObservableObject<Cacheable<Map<Integer, CatchType>>> res = dataService.fetchCatchTypes();
		
		res.initializedProperty().addListener((obs, oldVal, newVal) -> {
			future.complete(res.get().getData());
		});
		
		Map<Integer, CatchType> catchTypes;
		
		if (res.isInitialized()) {//If the data is fetched before the listener gets registered, get the results immediately
			catchTypes = res.get().getData();
		} else {//Otherwise, wait at least two seconds before giving up
			catchTypes = future.get(2, TimeUnit.SECONDS);
		}
		
		assertEquals(3, catchTypes.size());//Make sure 3 distinct catch types were fetched
		
		assertTrue(catchTypes.containsKey(2));
		
		CatchType oracle = new CatchType(2, "stoat", null);
		
		assertEquals(oracle, catchTypes.get(2));//Check one of the catch types to ensure it was fetched successfully
	}
	
	private void updateAndBlock (Trapline trapline) throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		GluonObservableObject<ParserTrapline> results = dataService.storeTrapline(trapline);
		results.addListener(obs -> {
			if (results.get() != null) {	
    			latch.countDown();
			}
    	});
        
		//Check that the data wasn't saved before we even got here, and if not, wait for it to save
        if (results.get() == null && !latch.await(5, TimeUnit.SECONDS)) {
        	LOG.log(Level.SEVERE, "Failed to save trapline. status="+results.stateProperty().get()+", results="+results.get(), results.exceptionProperty());
            throw new TimeoutException();
        }
	}

}
